package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.ProxiumPipeline;
import kr.rtustudio.bridge.proxium.api.RequestException;
import kr.rtustudio.bridge.proxium.api.ResponseHandler;
import kr.rtustudio.bridge.proxium.api.ResponseStatus;
import kr.rtustudio.bridge.proxium.api.protocol.internal.RequestPacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ResponsePacket;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    protected final Map<BridgeChannel, Consumer<Object>> channelHandlers =
            new ConcurrentHashMap<>();
    protected final Map<UUID, CompletableFuture<Object>> pendingRequests =
            new ConcurrentHashMap<>();
    protected final Map<BridgeChannel, Map<Class<?>, ResponseHandler<?, ?>>> responseHandlers =
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
    public final String getId() {
        return getServer();
    }

    @Override
    public void register(BridgeChannel channel, Class<?>... types) {
        options.register(channel, types);
        registeredChannels.add(channel);
        onChannelRegistered(channel);
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        channelHandlers.merge(channel, handler, Consumer::andThen);
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

        Consumer<Object> handler = channelHandlers.get(channel);
        if (handler == null) {
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

        try {
            handler.accept(decoded);
        } catch (Exception e) {
            log.error("Error in handler for channel: {}", channel, e);
        }
        return decoded;
    }

    /** 바이트 프레임의 채널만 조회한다. */
    @Nullable
    public BridgeChannel peekChannel(byte[] frame) {
        return options.peekChannel(frame);
    }

    @Override
    public <T, R> void respond(BridgeChannel channel, ResponseHandler<T, R> handler) {
        responseHandlers
                .computeIfAbsent(channel, k -> new ConcurrentHashMap<>())
                .put(Object.class, handler);
    }

    @Override
    public <T, R> void respond(
            BridgeChannel channel, Class<T> type, ResponseHandler<T, R> handler) {
        register(channel, type);
        responseHandlers
                .computeIfAbsent(channel, k -> new ConcurrentHashMap<>())
                .put(type, handler);
    }

    /**
     * 기본 RPC 패킷 처리기 (Request/Response).
     *
     * @param packetObj 수신된 패킷 객체
     * @return 처리가 완료되었으면 true, 일반 메세지 등 라우팅이 추가로 필요하면 false
     */
    @ApiStatus.Internal
    protected boolean handleRpcPacket(Object packetObj) {
        if (packetObj instanceof ResponsePacket response) {
            return handleIncomingResponse(response);
        } else if (packetObj instanceof RequestPacket request) {
            return handleIncomingRequest(request);
        }
        return false;
    }

    private boolean handleIncomingResponse(ResponsePacket response) {
        if (!java.util.Objects.equals(getServer(), response.target().name())) return false;

        CompletableFuture<Object> future = pendingRequests.remove(response.requestId());
        if (future == null) return true;

        switch (response.status()) {
            case SUCCESS -> future.complete(response.payload());
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
        if (!java.util.Objects.equals(getServer(), request.target().name())) return false;

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
                                    throw new java.util.concurrent.CompletionException(e);
                                }
                            })
                    .whenComplete(
                            (res, err) -> {
                                ResponseStatus status =
                                        err == null ? ResponseStatus.SUCCESS : ResponseStatus.ERROR;
                                sendResponse(request, status, res);
                            });
        } catch (Exception e) {
            log.error("Failed to execute response handler for channel {}", request.channel(), e);
            sendResponse(request, ResponseStatus.ERROR, null);
        }
        return true;
    }

    private void sendResponse(RequestPacket request, ResponseStatus status, Object payload) {
        ProxiumNode senderNode = new ProxiumNode(getServer(), null);
        send(
                ResponsePacket.builder()
                        .requestId(request.requestId())
                        .sender(senderNode)
                        .target(request.sender())
                        .channel(request.channel())
                        .status(status)
                        .payload(payload)
                        .build());
    }

    protected void onChannelRegistered(BridgeChannel channel) {}

    @Override
    public <T, R> CompletableFuture<R> request(
            ProxiumNode target, BridgeChannel channel, T request, Duration timeout) {
        UUID requestId = UUID.randomUUID();
        CompletableFuture<R> future = new CompletableFuture<>();

        @SuppressWarnings("unchecked")
        CompletableFuture<Object> objFuture = (CompletableFuture<Object>) (Object) future;
        pendingRequests.put(requestId, objFuture);

        RequestPacket packet =
                RequestPacket.builder()
                        .requestId(requestId)
                        .sender(new ProxiumNode(getServer(), null))
                        .target(target)
                        .channel(channel)
                        .payload(request)
                        .build();

        dispatchOutboundPacket(packet);
        return future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete(
                        (res, err) -> {
                            pendingRequests.remove(requestId);
                            if (err != null
                                    && err instanceof java.util.concurrent.TimeoutException) {
                                future.completeExceptionally(
                                        new RequestException(
                                                ResponseStatus.TIMEOUT,
                                                "RPC request timed out after "
                                                        + timeout.toMillis()
                                                        + "ms",
                                                err));
                            }
                        });
    }

    protected abstract void dispatchOutboundPacket(Object packet);
}
