package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.TransactionPacket;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyConnector;
import kr.rtustudio.bridge.proxium.core.configuration.ProxiumConfig;
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
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

/**
 * 프록시 측(Bungee/Velocity 등) Proxium 플랫폼 기반 클래스.
 *
 * <p>다수의 백엔드 서버 커넥션을 관리하고, 프로토콜별 아웃바운드 커넥터를 통해 서버와 통신한다.
 */
@Slf4j(topic = "Proxium")
@Getter
public abstract class ProxiumProxy extends AbstractProxium {

    private static final ConcurrentHashMap<String, ProxiumNode> serversByName =
            new ConcurrentHashMap<>();

    private final Map<Connection, Set<BridgeChannel>> serverSubscriptions =
            new ConcurrentHashMap<>();

    private final ProxiumConfig settings;
    private final Path dataFolder;

    protected ProxiumProxy(BridgeOptions options, Path dataFolder, ProxiumConfig settings) {
        super(options);
        this.dataFolder = dataFolder;
        this.settings = settings;
        ProxiumAPI.PROTOCOL_LOADED.register(this::startProtocol);
        ProxiumAPI.getLoadedProtocols().forEach(this::startProtocol);
    }

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

    protected static String addressKey(SocketAddress address) {
        if (address instanceof InetSocketAddress inetAddress) {
            String host =
                    (inetAddress.getAddress() != null)
                            ? inetAddress.getAddress().getHostAddress()
                            : inetAddress.getHostString();
            if ("localhost".equalsIgnoreCase(host)) {
                host = "127.0.0.1";
            }
            return host + ":" + inetAddress.getPort();
        }
        return String.valueOf(address);
    }

    public Optional<ProxiumNode> getProxiumNode(String name) {
        return Optional.ofNullable(serversByName.get(name));
    }

    public Optional<ProxiumNode> getProxiumNode(SocketAddress address) {
        String key = addressKey(address);
        return serversByName.values().stream()
                .filter(
                        server -> {
                            String nodeAddr = server.address();
                            if (nodeAddr == null) return false;
                            // ProxiumNode.address() stores InetSocketAddress.toString()
                            // (e.g. "/127.0.0.1:25566"). Normalize and compare.
                            String normalized =
                                    nodeAddr.replace("/", "").replace("localhost", "127.0.0.1");
                            return key.equals(normalized);
                        })
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

    @Override
    protected void dispatchOutboundPacket(Object packet) {
        routeBridgePacket(packet);
    }

    public void routeBridgePacket(Object packetObj) {
        if (handleRpcPacket(packetObj)) {
            return;
        }

        if (packetObj instanceof TransactionPacket transactionPacket) {
            String targetId = transactionPacket.target().name();

            // Route to target server
            ProxiumNode target = serversByName.get(targetId);
            if (target != null) {
                String targetKey =
                        target.address() != null
                                ? target.address()
                                        .replace("/", "")
                                        .replace("localhost", "127.0.0.1")
                                : null;
                Optional<Connection> connOpt =
                        serverSubscriptions.entrySet().stream()
                                .filter(
                                        e ->
                                                e.getValue().contains(BridgeChannel.INTERNAL)
                                                        && targetKey != null
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

    // ── 하위 클래스 훅 ──

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

    // ── 커넥션 매니저 (기존 core.proxy.ProxiumProxy 흡수) ──

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

        Consumer<Object> systemHandler =
                channelHandlers.get(BridgeChannel.of("rsf:system:connection"));
        if (systemHandler != null) {
            systemHandler.accept(connection);
        }
    }

    /** 백엔드 서버가 연결되었을 때의 플랫폼별 처리 (ProxiumNode 전송 등). */
    protected abstract void onServerConnected(Connection connection);

    /** 이 프록시의 등록된 서버 목록을 반환한다. */
    public abstract List<ProxiumNode> getProxiumNodes();

    private void startProtocol(Protocol protocol) {
        if (protocol.toString().equals(BridgeChannel.PROXIUM.toString())) return;
        for (ProxiumNode server : serversByName.values()) {
            String serverKey =
                    server.address() != null
                            ? server.address().replace("/", "").replace("localhost", "127.0.0.1")
                            : null;
            boolean hasProtocol =
                    serverSubscriptions.entrySet().stream()
                            .filter(
                                    e ->
                                            serverKey != null
                                                    && addressKey(e.getKey().getRemoteAddress())
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

    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_SECONDS = 5;
    private final ScheduledExecutorService retryScheduler =
            Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "Proxium-Retry");
                        t.setDaemon(true);
                        return t;
                    });

    private void connectToServer(Protocol protocol, ProxiumNode server) {
        connectToServer(protocol, server, 0, false);
    }

    private void connectToServer(
            Protocol protocol, ProxiumNode server, int attempt, boolean wasConnected) {
        String[] parts = server.address().replace("/", "").split(":");
        InetSocketAddress inetAddr = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        ProxyConnector connector =
                new ProxyConnector(
                        inetAddr,
                        dataFolder.resolve("Proxium").toAbsolutePath().toString(),
                        settings.getTls().isEnabled());
        java.util.concurrent.atomic.AtomicBoolean connected =
                new java.util.concurrent.atomic.AtomicBoolean(wasConnected);
        connector
                .onConnectionEstablished(conn -> connected.set(true))
                .onConnectionLost(
                        conn -> {
                            if (connected.get()) {
                                // 이전에 연결이 성공했었으면 재시도 카운터 리셋 → 무한 재연결
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
            String removedKey =
                    removed.address() != null
                            ? removed.address().replace("/", "").replace("localhost", "127.0.0.1")
                            : null;
            serverSubscriptions.keySet().stream()
                    .filter(
                            c ->
                                    removedKey != null
                                            && addressKey(c.getRemoteAddress()).equals(removedKey)
                                            && c.isOpen())
                    .forEach(Connection::disconnect);
        }
    }
}
