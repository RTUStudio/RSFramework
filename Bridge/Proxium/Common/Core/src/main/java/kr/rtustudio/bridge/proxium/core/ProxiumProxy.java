package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.RequestPacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ResponsePacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ServerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.TransactionPacket;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyConnector;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.ApiStatus;

/**
 * 프록시 측 Proxium 플랫폼 기반 클래스.
 *
 * <p>다수의 백엔드 서버 커넥션을 관리하고, 프로토콜별 아웃바운드 커넥터를 통해 서버와 통신한다.
 */
@Slf4j(topic = "Proxium")
@Getter
public abstract class ProxiumProxy extends AbstractProxium {

    private final ConcurrentHashMap<String, ProxiumNode> serversByName = new ConcurrentHashMap<>();

    private final Map<Connection, Set<BridgeChannel>> serverSubscriptions =
            new ConcurrentHashMap<>();
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public boolean isShuttingDown() {
        return shuttingDown.get();
    }

    private final ProxiumConfig settings;
    private final Path dataFolder;
    private final ScheduledExecutorService retryScheduler =
            Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "Proxium-Retry");
                        t.setDaemon(true);
                        return t;
                    });

    protected ProxiumProxy(BridgeOptions options, Path dataFolder, ProxiumConfig settings) {
        super(options);
        this.dataFolder = dataFolder;
        this.settings = settings;
        ProxiumAPI.PROTOCOL_LOADED.register(this::startProtocol);
        ProxiumAPI.getLoadedProtocols().forEach(this::startProtocol);
    }

    /** SocketAddress를 정규화된 \"host:port\" 문자열로 변환한다. */
    public static String addressKey(SocketAddress address) {
        if (address instanceof InetSocketAddress inetAddress) {
            String host =
                    (inetAddress.getAddress() != null)
                            ? inetAddress.getAddress().getHostAddress()
                            : inetAddress.getHostString();
            return host.replace("localhost", "127.0.0.1") + ":" + inetAddress.getPort();
        }
        return String.valueOf(address);
    }

    @Override
    public Duration getRequestTimeout() {
        return settings.getRequestTimeout();
    }

    /**
     * Protocol.Builder에 커넥션 핸들러, 인증 핸들러 등 플랫폼별 설정을 적용한다.
     *
     * @param builder 프로토콜 빌더
     */
    protected abstract void configureProtocol(Protocol.Builder builder);

    /**
     * 프록시측 INTERNAL Protocol을 생성하고 로드한다.
     *
     * @param config 프로토콜 설정 (압축, 패킷 크기)
     */
    protected void start(ProxiumConfig config) {
        Protocol.Builder builder = createProtocol(BridgeChannel.INTERNAL, config);
        configureProtocol(builder);
        builder.load();
    }

    // ── 주소 정규화 ──

    /**
     * 채널별 Protocol을 생성하고 로드한다.
     *
     * @param channel 새로 등록된 채널
     */
    protected void startChannelProtocol(BridgeChannel channel) {
        Protocol.Builder builder = createProtocol(channel, settings);
        configureProtocol(builder);
        builder.load();
    }

    // ── 서버 조회 ──

    @ApiStatus.Internal
    public Map<Connection, Set<BridgeChannel>> getServerSubscriptions() {
        return serverSubscriptions;
    }

    public List<Connection> getConnectedServers() {
        return serverSubscriptions.keySet().stream().filter(Connection::isOpen).toList();
    }

    public Connection getConnection(SocketAddress address) {
        String targetAddress = addressKey(address);
        return serverSubscriptions.keySet().stream()
                .filter(server -> addressKey(server.getRemoteAddress()).equals(targetAddress))
                .findFirst()
                .orElse(null);
    }

    public Optional<ProxiumNode> getProxiumNode(String name) {
        return Optional.ofNullable(serversByName.get(name));
    }

    public Optional<ProxiumNode> getProxiumNode(SocketAddress address) {
        String key = addressKey(address);
        return serversByName.values().stream()
                .filter(server -> key.equals(addressKey(server.getSocketAddress())))
                .findFirst();
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public ProxiumNode getNode(String name) {
        return serversByName.get(name);
    }

    // ── 패킷 라우팅 ──

    @Override
    protected void dispatchOutboundPacket(Object packet) {
        if (packet instanceof TransactionPacket txn) {
            // Transaction 패킷은 자기 채널로 encode하여 대상 서버에 직접 전송
            String targetName = txn.target().name();
            ProxiumNode target = serversByName.get(targetName);
            if (target != null) {
                byte[] frame = options.encode(txn.channel(), packet);
                String targetKey = addressKey(target.getSocketAddress());
                serverSubscriptions.entrySet().stream()
                        .filter(e -> addressKey(e.getKey().getRemoteAddress()).equals(targetKey))
                        .map(Map.Entry::getKey)
                        .filter(Connection::isOpen)
                        .findFirst()
                        .ifPresentOrElse(
                                conn -> conn.send(frame),
                                () ->
                                        log.warn(
                                                "Dropped Transaction packet for offline target '{}'",
                                                targetName));
            } else {
                log.warn("Dropped Transaction packet for unknown target '{}'", targetName);
            }
        } else {
            // 일반 패킷은 기존대로 INTERNAL 브로드캐스트
            byte[] frame = options.encode(BridgeChannel.INTERNAL, packet);
            getConnectedServers().forEach(conn -> conn.send(frame));
        }
    }

    /**
     * TransactionPacket에 sender를 채워 넣고 target 서버로 릴레이한다. 프록시는 payload(byte[])를 디코딩하지 않는다.
     *
     * @param sourceConnection 패킷을 보낸 서버의 Connection (sender 확인용)
     * @param txn 디코딩된 TransactionPacket (sender는 null)
     */
    public void relayTransactionPacket(Connection sourceConnection, TransactionPacket txn) {
        String targetName = txn.target().name();

        // target이 프록시 자신이면 직접 처리
        if (Objects.equals(getName(), targetName)) {
            handleTransaction(txn);
            return;
        }

        // sender 결정: 패킷을 보낸 Connection의 주소로 ProxiumNode 조회
        ProxiumNode senderNode = getProxiumNode(sourceConnection.getRemoteAddress()).orElse(null);

        // sender가 채워진 패킷 재구성
        Object annotatedPacket;
        if (txn instanceof RequestPacket req) {
            annotatedPacket =
                    RequestPacket.builder()
                            .requestId(req.requestId())
                            .sender(senderNode)
                            .target(req.target())
                            .channel(req.channel())
                            .payload(req.payload())
                            .build();
        } else if (txn instanceof ResponsePacket resp) {
            annotatedPacket =
                    ResponsePacket.builder()
                            .requestId(resp.requestId())
                            .sender(senderNode)
                            .target(resp.target())
                            .channel(resp.channel())
                            .status(resp.status())
                            .payload(resp.payload())
                            .build();
        } else {
            return;
        }

        // 재인코딩 후 target 서버로 전달
        byte[] newFrame = options.encode(txn.channel(), annotatedPacket);
        ProxiumNode target = serversByName.get(targetName);
        if (target != null) {
            String targetKey = addressKey(target.getSocketAddress());
            serverSubscriptions.entrySet().stream()
                    .filter(e -> addressKey(e.getKey().getRemoteAddress()).equals(targetKey))
                    .map(Map.Entry::getKey)
                    .filter(Connection::isOpen)
                    .findFirst()
                    .ifPresentOrElse(
                            conn -> conn.send(newFrame),
                            () ->
                                    log.warn(
                                            "Dropped Transaction packet for offline target '{}'",
                                            targetName));
        } else {
            log.warn("Dropped Transaction packet for unknown target '{}'", targetName);
        }
    }

    // ── 커넥션 라이프사이클 ──

    @Override
    public boolean isConnected() {
        return !getConnectedServers().isEmpty();
    }

    @Override
    public boolean send(@NonNull Object packet) {
        byte[] frame = options.encode(BridgeChannel.INTERNAL, packet);
        List<Connection> connectedServers = getConnectedServers();
        if (connectedServers.isEmpty()) return false;
        connectedServers.forEach(conn -> conn.send(frame));
        return true;
    }

    @Override
    public void publish(BridgeChannel channel, Object message) {
        if (!registeredChannels.contains(channel)) {
            log.warn(
                    "No codec registered for channel: {}. Call register() before publish().",
                    channel);
            return;
        }
        byte[] frame = options.encode(channel, message);
        getConnectedServers().forEach(conn -> conn.send(frame));
    }

    @Override
    public void close() {
        shutdown();
        channelHandlers.clear();
        registeredChannels.clear();
    }

    @Override
    public void ready(Connection connection) {
        if (!connection.isOpen()) return;

        String remoteAddr = addressKey(connection.getRemoteAddress());
        ProxiumNode node =
                serversByName.values().stream()
                        .filter(s -> remoteAddr.equals(addressKey(s.getSocketAddress())))
                        .findFirst()
                        .orElse(null);
        if (node == null) return;

        connection.send(options.encode(BridgeChannel.INTERNAL, node));
        connection.send(
                options.encode(BridgeChannel.INTERNAL, new ServerList(Map.copyOf(serversByName))));
        if (!players.isEmpty()) {
            connection.send(options.encode(BridgeChannel.INTERNAL, new PlayerList(players)));
        }
    }

    // ── 프로토콜 커넥터 ──

    private void startProtocol(Protocol protocol) {
        if (protocol.getChannel().equals(BridgeChannel.PROXIUM)) return;
        for (ProxiumNode server : serversByName.values()) {
            String serverKey = addressKey(server.getSocketAddress());
            boolean hasProtocol =
                    serverSubscriptions.entrySet().stream()
                            .filter(
                                    e ->
                                            addressKey(e.getKey().getRemoteAddress())
                                                    .equals(serverKey))
                            .anyMatch(
                                    e ->
                                            e.getKey()
                                                    .getProtocol()
                                                    .toString()
                                                    .equals(protocol.toString()));

            if (!hasProtocol) {
                connectToServer(protocol, server);
            }
        }
    }

    private void connectToServer(Protocol protocol, ProxiumNode server) {
        connectToServer(protocol, server, 0);
    }

    private void connectToServer(Protocol protocol, ProxiumNode server, long attempt) {
        ProxyConnector connector =
                new ProxyConnector(
                        server.getSocketAddress(),
                        dataFolder.resolve("Proxium").toAbsolutePath().toString(),
                        settings.getTls().isEnabled());
        long maxRetries = settings.getMaxRetries();
        AtomicBoolean connected = new AtomicBoolean(false);
        connector
                .onConnectionEstablished(conn -> connected.set(true))
                .onConnectionLost(
                        conn -> {
                            if (shuttingDown.get()) return;

                            long retryDelay = settings.getRetryDelaySeconds();

                            if (connected.get()) {
                                // 연결 후 끊김 → attempt 리셋 후 재시도
                                retryScheduler.schedule(
                                        () -> connectToServer(protocol, server, 0),
                                        retryDelay,
                                        TimeUnit.SECONDS);
                            } else if (attempt + 1 < maxRetries) {
                                // 연결 재시도
                                log.debug(
                                        "Server '{}' not reachable, retrying in {}s ({}/{})",
                                        server.name(),
                                        retryDelay,
                                        attempt + 2,
                                        maxRetries == Long.MAX_VALUE ? "∞" : maxRetries);
                                retryScheduler.schedule(
                                        () -> connectToServer(protocol, server, attempt + 1),
                                        retryDelay,
                                        TimeUnit.SECONDS);
                            } else {
                                log.warn(
                                        "Server '{}' is not reachable after {} attempts",
                                        server.name(),
                                        maxRetries);
                            }
                        })
                .connect(protocol);
    }

    // ── 서버 등록/해제 ──

    @ApiStatus.Internal
    public void shutdown() {
        shuttingDown.set(true);
        retryScheduler.shutdownNow();
        serverSubscriptions
                .keySet()
                .forEach(
                        conn -> {
                            if (conn.isOpen()) {
                                conn.disconnect();
                            }
                        });
        serverSubscriptions.clear();
        serversByName.clear();
    }

    @ApiStatus.Internal
    public void registerServer(ProxiumNode server) {
        if (serversByName.putIfAbsent(server.name(), server) == null) {
            broadcastServerList();
            for (Protocol protocol : ProxiumAPI.getLoadedProtocols()) {
                if (protocol.getChannel().equals(BridgeChannel.PROXIUM)) continue;
                connectToServer(protocol, server);
            }
        }
    }

    @ApiStatus.Internal
    public void unregisterServer(ProxiumNode server) {
        ProxiumNode removed = serversByName.remove(server.name());
        if (removed != null) {
            broadcastServerList();
            String removedKey = addressKey(removed.getSocketAddress());
            serverSubscriptions.keySet().stream()
                    .filter(c -> addressKey(c.getRemoteAddress()).equals(removedKey) && c.isOpen())
                    .forEach(Connection::disconnect);
        }
    }

    private void broadcastServerList() {
        byte[] frame =
                options.encode(BridgeChannel.INTERNAL, new ServerList(Map.copyOf(serversByName)));
        getConnectedServers().forEach(conn -> conn.send(frame));
    }
}
