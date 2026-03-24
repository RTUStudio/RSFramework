package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.Node;
import kr.rtustudio.bridge.context.RequestContext;
import kr.rtustudio.bridge.context.ResponseContext;
import kr.rtustudio.bridge.context.ResponseStatus;
import kr.rtustudio.bridge.exception.RequestException;
import kr.rtustudio.bridge.handler.ResponseHandler;
import kr.rtustudio.bridge.proxium.api.ProxiumPipeline;
import kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.RequestPacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ResponsePacket;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

@Slf4j(topic = "Proxium")
@Getter
@RequiredArgsConstructor
public abstract class AbstractProxium implements ProxiumPipeline {

    protected final BridgeOptions options;
    protected final Set<BridgeChannel> registeredChannels = ConcurrentHashMap.newKeySet();
    protected final Map<BridgeChannel, Map<Class<?>, Consumer<?>>> channelHandlers =
            new ConcurrentHashMap<>();
    protected final Map<UUID, CompletableFuture<Object[]>> pendingRequests =
            new ConcurrentHashMap<>();
    protected final Map<BridgeChannel, Map<Class<?>, ResponseHandler<?, ?>>> responseHandlers =
            new ConcurrentHashMap<>();
    protected final Map<BridgeChannel, Consumer<RequestException>> respondErrorHandlers =
            new ConcurrentHashMap<>();

    protected final Map<UUID, ProxyPlayer> players = new ConcurrentHashMap<>();

    @Override
    public @NotNull Map<UUID, ProxyPlayer> getPlayers() {
        return Map.copyOf(players);
    }

    @Override
    public ProxyPlayer getPlayer(UUID uniqueId) {
        return players.get(uniqueId);
    }

    @Override
    public void register(BridgeChannel channel, Class<?>... types) {
        options.register(channel, types);
        if (registeredChannels.add(channel)) {
            onChannelRegistered(channel);
        }
    }

    /**
     * Protocol.Builder를 생성하고, 공통 설정 적용 및 채널 타입 등록을 한 번에 수행한다.
     *
     * @param channel 프로토콜 채널
     * @param config Proxium 설정
     * @param types 채널에 등록할 코덱 타입들
     * @return 설정이 적용된 Protocol.Builder
     */
    protected Protocol.Builder createProtocol(
            BridgeChannel channel, ProxiumConfig config, Class<?>... types) {
        Protocol.Builder protocol = Protocol.create(channel);
        protocol.setOptions(options);
        protocol.setCompression(config.getCompression());
        protocol.setMaxPacketSize(config.getMaxPacketSize());
        if (types.length > 0) {
            register(channel, types);
        }
        return protocol;
    }

    @Override
    public <T> void subscribe(BridgeChannel channel, Class<T> type, Consumer<T> handler) {
        register(channel, type);
        channelHandlers.computeIfAbsent(channel, k -> new ConcurrentHashMap<>()).put(type, handler);
    }

    @Override
    public void unsubscribe(BridgeChannel channel) {
        channelHandlers.remove(channel);
        registeredChannels.remove(channel);
    }

    /**
     * 바이트 프레임을 디코딩하고, 해당 채널의 핸들러에 디스패치한다.
     *
     * @return 디코딩된 객체 (핸들러가 없거나 채널이 null이면 null)
     */
    @Nullable
    public Object dispatchPacket(byte[] frame) {
        BridgeChannel channel = options.peekChannel(frame);
        if (channel == null) {
            log.warn("Received unroutable Proxium frame");
            return null;
        }

        Map<Class<?>, Consumer<?>> handlers = channelHandlers.get(channel);
        if (handlers == null) {
            return null; // 구독자가 없으면 원본 바이트 배열을 파싱하지 않고 null 반환 -> ProxyConnectionHandler 에서 순수 바이트
            // 릴레이 스위칭 처리됨
        }

        Object decoded;
        try {
            decoded = options.decode(frame);
        } catch (Exception e) {
            log.error("Failed to decode frame on channel: {}", channel, e);
            return null;
        }

        // 타입별 핸들러 매칭: 정확한 타입 → Object.class 폴백
        @SuppressWarnings("unchecked")
        Consumer<Object> handler = (Consumer<Object>) handlers.get(decoded.getClass());
        if (handler == null) {
            //noinspection unchecked
            handler = (Consumer<Object>) handlers.get(Object.class);
        }

        if (handler != null) {
            try {
                handler.accept(decoded);
            } catch (Exception e) {
                log.error("Error in handler for channel: {}", channel, e);
            }
        }
        return decoded;
    }

    /** 바이트 프레임의 채널만 조회한다. */
    @Nullable
    public BridgeChannel peekChannel(byte[] frame) {
        return options.peekChannel(frame);
    }

    @Override
    public ResponseContext respond(BridgeChannel channel) {
        return new ResponseContext(
                (type, handler) -> {
                    register(channel, type);
                    responseHandlers
                            .computeIfAbsent(channel, k -> new ConcurrentHashMap<>())
                            .put(type, handler);
                    log.info("Channel respond registered: {} [{}]", channel, type.getSimpleName());
                },
                errorHandler -> respondErrorHandlers.put(channel, errorHandler));
    }

    /**
     * 트랜잭션 패킷 처리기 (Request/Response).
     *
     * @param packetObj 수신된 패킷 객체
     * @return 처리가 완료되었으면 true, 일반 메세지 등 라우팅이 추가로 필요하면 false
     */
    @ApiStatus.Internal
    protected boolean handleTransaction(Object packetObj) {
        if (packetObj instanceof ResponsePacket response) {
            return handleIncomingResponse(response);
        } else if (packetObj instanceof RequestPacket request) {
            return handleIncomingRequest(request);
        }
        return false;
    }

    private boolean handleIncomingResponse(ResponsePacket response) {
        if (!Objects.equals(getName(), response.target())) return false;

        CompletableFuture<Object[]> future = pendingRequests.remove(response.requestId());
        if (future == null) return true;

        switch (response.status()) {
            case SUCCESS -> future.complete(new Object[] {response.sender(), response.payload()});
            case NO_HANDLER ->
                    future.completeExceptionally(
                            new RequestException(
                                    ResponseStatus.NO_HANDLER,
                                    "No ResponseHandler registered for channel: "
                                            + response.channel()));
            case ERROR ->
                    future.completeExceptionally(
                            new RequestException(
                                    ResponseStatus.ERROR,
                                    "Error processing request on target server"));
            default ->
                    future.completeExceptionally(
                            new RequestException(
                                    response.status(),
                                    "RPC failed with status: " + response.status()));
        }
        return true;
    }

    private boolean handleIncomingRequest(RequestPacket request) {
        if (!Objects.equals(getName(), request.target())) return false;

        Map<Class<?>, ResponseHandler<?, ?>> handlers = responseHandlers.get(request.channel());
        if (handlers == null || handlers.isEmpty()) {
            sendResponse(request, ResponseStatus.NO_HANDLER, null);
            return true;
        }

        // 타입별 핸들러 매칭: 정확한 타입 → Object.class 폴백
        ResponseHandler<?, ?> raw = handlers.get(request.payload().getClass());
        if (raw == null) raw = handlers.get(Object.class);
        if (raw == null) {
            sendResponse(request, ResponseStatus.NO_HANDLER, null);
            return true;
        }

        @SuppressWarnings("unchecked")
        ResponseHandler<Object, Object> handler = (ResponseHandler<Object, Object>) raw;

        try {
            CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    return handler.handle(request.sender(), request.payload());
                                } catch (Exception e) {
                                    throw new CompletionException(e);
                                }
                            })
                    .whenComplete(
                            (res, err) -> {
                                ResponseStatus status =
                                        err == null ? ResponseStatus.SUCCESS : ResponseStatus.ERROR;
                                sendResponse(request, status, res);
                                if (err != null) {
                                    invokeRespondErrorHandler(request.channel(), err);
                                }
                            });
        } catch (Exception e) {
            log.error("Failed to execute response handler for channel {}", request.channel(), e);
            sendResponse(request, ResponseStatus.ERROR, null);
            invokeRespondErrorHandler(request.channel(), e);
        }
        return true;
    }

    private void sendResponse(RequestPacket request, ResponseStatus status, Object payload) {
        send(
                ResponsePacket.builder()
                        .requestId(request.requestId())
                        .sender(getName())
                        .target(request.sender())
                        .channel(request.channel())
                        .status(status)
                        .payload(payload)
                        .build());
    }

    protected void onChannelRegistered(BridgeChannel channel) {}

    private void invokeRespondErrorHandler(BridgeChannel channel, Throwable err) {
        Consumer<RequestException> errorHandler = respondErrorHandlers.get(channel);
        Throwable cause = err instanceof CompletionException ? err.getCause() : err;
        RequestException exception =
                cause instanceof RequestException re
                        ? re
                        : new RequestException(ResponseStatus.ERROR, cause.getMessage(), cause);
        if (errorHandler != null) {
            try {
                errorHandler.accept(exception);
            } catch (Exception e) {
                log.error("Error in respond error handler for channel {}", channel, e);
            }
        } else {
            log.error("Error in respond handler for channel {}", channel, cause);
        }
    }

    @Override
    public <T> RequestContext request(
            Node target, BridgeChannel channel, T request, Duration timeout) {
        register(channel, request.getClass());
        UUID requestId = UUID.randomUUID();
        CompletableFuture<Object[]> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        RequestPacket packet =
                RequestPacket.builder()
                        .requestId(requestId)
                        .sender(getName())
                        .target(target.name())
                        .channel(channel)
                        .payload(request)
                        .build();

        dispatchOutboundPacket(packet);
        if (!timeout.isZero() && !timeout.isNegative()) {
            future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        future.whenComplete((res, err) -> pendingRequests.remove(requestId));

        return new RequestContext(future, type -> register(channel, type));
    }

    protected abstract void dispatchOutboundPacket(Object packet);
}
