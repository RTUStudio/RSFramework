package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyConnector;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.RegisteredServer;
import kr.rtustudio.bridge.proxium.core.config.ProxiumSettings;
import kr.rtustudio.bridge.proxium.core.handler.ProxyConnectionHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * 프록시 측(Bungee/Velocity 등) Proxium 플랫폼 기반 클래스.
 *
 * <p>다수의 백엔드 서버 커넥션을 관리하고, 프로토콜별 아웃바운드 커넥터를 통해 서버와 통신한다.
 */
@Slf4j(topic = "Proxium")
public abstract class ProxiumProxy extends AbstractProxium implements Proxium {

    private static final ConcurrentHashMap<RegisteredServer, CopyOnWriteArrayList<ProxyConnector>>
            servers = new ConcurrentHashMap<>();

    protected final Map<UUID, ProxyPlayer> players = new ConcurrentHashMap<>();
    @Getter private final ProxiumSettings settings;
    private final Path dataFolder;

    protected ProxiumProxy(
            BridgeOptions options,
            Path dataFolder,
            ProxiumSettings settings,
            List<RegisteredServer> initialServers) {
        super(options);
        this.dataFolder = dataFolder;
        this.settings = settings;
        initialServers.forEach(server -> servers.put(server, new CopyOnWriteArrayList<>()));
        ProxiumProxy.setServerPollRate(settings.getServerPollRate());
        ProxiumAPI.PROTOCOL_LOADED.register(this::startProtocol);
        ProxiumAPI.getLoadedProtocols().forEach(this::startProtocol);
    }

    private static int serverPollRate = 5000;

    public static void setServerPollRate(int rate) {
        serverPollRate = rate;
    }

    // ── Proxium 구현 ──

    public static List<RegisteredServer> getRegisteredServerList() {
        return servers.keySet().stream().toList();
    }

    public static Optional<RegisteredServer> findRegisteredServer(String name) {
        return getRegisteredServerList().stream().filter(s -> s.name().equals(name)).findFirst();
    }

    public static Optional<RegisteredServer> findRegisteredServer(SocketAddress address) {
        return getRegisteredServerList().stream()
                .filter(s -> s.address().equals(address))
                .findFirst();
    }

    public static List<RegisteredServer> getConnectedServers(@NonNull Protocol protocol) {
        return servers.entrySet().stream()
                .filter(
                        e ->
                                e.getValue().stream()
                                        .anyMatch(
                                                c ->
                                                        c.isConnected()
                                                                && protocol.equals(
                                                                        c.getCurrentProtocol())))
                .map(Map.Entry::getKey)
                .toList();
    }

    public static Optional<RegisteredServer> getConnectedServer(
            @NonNull Protocol protocol, String name) {
        return getConnectedServers(protocol).stream()
                .filter(s -> s.name().equals(name))
                .findFirst();
    }

    public static Optional<RegisteredServer> getConnectedServer(
            @NonNull Protocol protocol, SocketAddress address) {
        return getConnectedServers(protocol).stream()
                .filter(s -> s.address().equals(address))
                .findFirst();
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    // ── 하위 클래스 훅 ──

    @Override
    public boolean isConnected() {
        return !ProxyConnectionHandler.getServers().isEmpty();
    }

    @Override
    public Map<UUID, ProxyPlayer> getPlayers() {
        return Map.copyOf(players);
    }

    @Override
    public boolean send(@NonNull Object packet) {
        byte[] frame = options.encode(BridgeChannel.INTERNAL, packet);
        List<Connection> connectedServers = ProxyConnectionHandler.getServers();
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
        ProxyConnectionHandler.getServers().forEach(conn -> conn.send(frame));
    }

    @Override
    public void close() {
        ProxyConnectionHandler.getServers()
                .forEach(
                        conn ->
                                conn.send(
                                        options.encode(
                                                BridgeChannel.INTERNAL,
                                                createStandaloneServerName())));
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

    /** 백엔드 서버가 연결되었을 때의 플랫폼별 처리 (ServerName 전송 등). */
    protected abstract void onServerConnected(Connection connection);

    /** 이 프록시의 등록된 서버 목록을 반환한다. */
    public abstract List<RegisteredServer> getRegisteredServers();

    /** close() 시 전송할 Standalone ServerName 패킷 객체를 생성한다. */
    protected abstract Object createStandaloneServerName();

    private void startProtocol(Protocol protocol) {
        if (protocol.toString().equals(BridgeChannel.PROXIUM.toString())) return;
        servers.forEach(
                (server, connectors) -> {
                    for (ProxyConnector connector : connectors) {
                        if (connector.getCurrentProtocol().toString().equals(protocol.toString()))
                            return;
                    }
                    connectToServer(protocol, server, connectors);
                });
    }

    private void connectToServer(
            Protocol protocol,
            RegisteredServer server,
            CopyOnWriteArrayList<ProxyConnector> connectors) {
        ProxyConnector connector =
                new ProxyConnector(
                        (InetSocketAddress) server.address(),
                        dataFolder.resolve("Proxium").toAbsolutePath().toString());
        connector
                .connect(protocol)
                .onConnectionLost(
                        connection -> {
                            connectors.remove(connector);

                            if (connection.getDisconnecter().equals(Side.PROXY)) return;
                            Thread.sleep(serverPollRate);
                            connectToServer(protocol, server, connectors);
                        });
        connectors.add(connector);
    }

    @ApiStatus.Internal
    public void shutdown() {
        servers.values().forEach(connectors -> connectors.forEach(ProxyConnector::disconnect));
        servers.clear();
    }

    @ApiStatus.Internal
    public void registerServer(RegisteredServer server) {
        if (servers.putIfAbsent(server, new CopyOnWriteArrayList<>()) == null)
            ProxiumAPI.getLoadedProtocols().forEach(this::startProtocol);
    }

    @ApiStatus.Internal
    public void unregisterServer(RegisteredServer server) {
        Optional.ofNullable(servers.remove(server))
                .ifPresent(connectors -> connectors.forEach(ProxyConnector::disconnect));
    }

    @Nullable
    public RegisteredServer getServer(InetSocketAddress address) {
        return servers.keySet().stream()
                .filter(server -> server.address().equals(address))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public RegisteredServer getServer(String name) {
        return servers.keySet().stream()
                .filter(server -> server.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
