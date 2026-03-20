package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.TransactionPacket;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyConnector;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.ApiStatus;

/**
 * 프록시 측(Bungee/Velocity 등) Proxium 플랫폼 기반 클래스.
 *
 * <p>다수의 백엔드 서버 커넥션을 관리하고, 프로토콜별 아웃바운드 커넥터를 통해 서버와 통신한다.
 */
@Slf4j(topic = "Proxium")
@Getter
public abstract class ProxiumProxy extends AbstractProxium {

    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_SECONDS = 5;

    private final ConcurrentHashMap<String, ProxiumNode> serversByName =
            new ConcurrentHashMap<>();

    private final Map<Connection, Set<BridgeChannel>> serverSubscriptions =
            new ConcurrentHashMap<>();
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

    @Override
    public java.time.Duration getRequestTimeout() {
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

    // ── 주소 정규화 ──

    /** SocketAddress를 정규화된 "host:port" 문자열로 변환한다. */
    protected static String addressKey(SocketAddress address) {
        if (address instanceof InetSocketAddress inetAddress) {
            String host =
                    (inetAddress.getAddress() != null)
                            ? inetAddress.getAddress().getHostAddress()
                            : inetAddress.getHostString();
            return host.replace("localhost", "127.0.0.1") + ":" + inetAddress.getPort();
        }
        return String.valueOf(address);
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
    public ProxiumNode getServer(String name) {
        return serversByName.get(name);
    }

    // ── 패킷 라우팅 ──

    @Override
    protected void dispatchOutboundPacket(Object packet) {
        routeBridgePacket(packet);
    }

    public void routeBridgePacket(Object packetObj) {
        if (handleTransaction(packetObj)) return;

        if (packetObj instanceof TransactionPacket transactionPacket) {
            String targetId = transactionPacket.target();
            ProxiumNode target = serversByName.get(targetId);

            if (target != null) {
                String targetKey = addressKey(target.getSocketAddress());
                Optional<Connection> connOpt =
                        serverSubscriptions.entrySet().stream()
                                .filter(
                                        e ->
                                                e.getValue().contains(BridgeChannel.INTERNAL)
                                                        && addressKey(e.getKey().getRemoteAddress())
                                                                .equals(targetKey))
                                .map(Map.Entry::getKey)
                                .filter(Connection::isOpen)
                                .findFirst();

                if (connOpt.isPresent()) {
                    connOpt.get().send(options.encode(BridgeChannel.INTERNAL, packetObj));
                    return;
                }
            }

            log.warn("Dropped routable Proxium packet intended for offline target '{}'", targetId);
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
        onServerConnected(connection);
    }

    /** 백엔드 서버가 연결되었을 때의 플랫폼별 처리 (ProxiumNode 전송 등). */
    protected abstract void onServerConnected(Connection connection);

    /** 이 프록시의 등록된 서버 목록을 반환한다. */
    public abstract List<ProxiumNode> getProxiumNodes();

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
        connectToServer(protocol, server, 0, false);
    }

    private void connectToServer(
            Protocol protocol, ProxiumNode server, int attempt, boolean wasConnected) {
        ProxyConnector connector =
                new ProxyConnector(
                        server.getSocketAddress(),
                        dataFolder.resolve("Proxium").toAbsolutePath().toString(),
                        settings.getTls().isEnabled());
        AtomicBoolean connected = new AtomicBoolean(wasConnected);
        connector
                .onConnectionEstablished(conn -> connected.set(true))
                .onConnectionLost(
                        conn -> {
                            if (connected.get()) {
                                log.debug(
                                        "Lost connection to '{}', reconnecting in {}s",
                                        server.name(),
                                        RETRY_DELAY_SECONDS);
                                retryScheduler.schedule(
                                        () -> connectToServer(protocol, server, 0, true),
                                        RETRY_DELAY_SECONDS,
                                        TimeUnit.SECONDS);
                            } else if (attempt + 1 < MAX_RETRIES) {
                                log.debug(
                                        "Connection to '{}' failed, retrying in {}s ({}/{})",
                                        server.name(),
                                        RETRY_DELAY_SECONDS,
                                        attempt + 2,
                                        MAX_RETRIES);
                                retryScheduler.schedule(
                                        () -> connectToServer(protocol, server, attempt + 1, false),
                                        RETRY_DELAY_SECONDS,
                                        TimeUnit.SECONDS);
                            } else {
                                log.warn(
                                        "Failed to connect to '{}' after {} attempts",
                                        server.name(),
                                        MAX_RETRIES);
                            }
                        })
                .connect(protocol);
    }

    // ── 서버 등록/해제 ──

    @ApiStatus.Internal
    public void shutdown() {
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
            ProxiumAPI.getLoadedProtocols().forEach(this::startProtocol);
        }
    }

    @ApiStatus.Internal
    public void unregisterServer(ProxiumNode server) {
        ProxiumNode removed = serversByName.remove(server.name());
        if (removed != null) {
            String removedKey = addressKey(removed.getSocketAddress());
            serverSubscriptions.keySet().stream()
                    .filter(c -> addressKey(c.getRemoteAddress()).equals(removedKey) && c.isOpen())
                    .forEach(Connection::disconnect);
        }
    }
}
