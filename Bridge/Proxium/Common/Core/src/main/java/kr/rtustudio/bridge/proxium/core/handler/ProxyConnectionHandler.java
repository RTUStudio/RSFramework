package kr.rtustudio.bridge.proxium.core.handler;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.core.ProxiumProxy;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Bungee/Velocity 프록시 측 공통 ConnectionHandler. 연결된 서버 추적, 채널 구독 관리, 패킷 중계 로직을 통합한다. */
@Slf4j(topic = "Proxium")
public class ProxyConnectionHandler implements ConnectionHandler {

    private static final Map<Connection, Set<BridgeChannel>> servers = new ConcurrentHashMap<>();

    private final ProxiumProxy proxium;

    public ProxyConnectionHandler(ProxiumProxy proxium) {
        this.proxium = proxium;
    }

    public static Connection getServer(SocketAddress address) {
        String targetAddress = addressKey(address);
        return servers.keySet().stream()
                .filter(server -> addressKey(server.getRemoteAddress()).equals(targetAddress))
                .findFirst()
                .orElse(null);
    }

    private static String addressKey(SocketAddress address) {
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

    public static List<Connection> getServers() {
        return servers.keySet().stream().filter(Connection::isOpen).toList();
    }

    @Override
    public void onReady(Connection connection) {
        servers.put(connection, ConcurrentHashMap.newKeySet());
        logConnection(connection);
        proxium.ready(connection);
    }

    @Override
    public void onDisconnect(Connection connection) {
        servers.remove(connection);
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (!(packet instanceof byte[] frame)) return;

        BridgeChannel channel = proxium.peekChannel(frame);
        if (channel == null) return;

        Object decoded = proxium.dispatchPacket(frame);

        if (decoded instanceof BridgeChannel subscribedChannel) {
            Set<BridgeChannel> subs = servers.get(connection);
            if (subs != null) subs.add(subscribedChannel);
            return;
        }

        getServers().stream()
                .filter(conn -> !conn.equals(connection))
                .filter(
                        conn -> {
                            Set<BridgeChannel> subs = servers.get(conn);
                            return subs != null && subs.contains(channel);
                        })
                .forEach(conn -> conn.send(frame));
    }

    protected void logConnection(Connection connection) {
        log.info("Connected to Server");
        log.info("┠ Address: {}", connection.getRemoteAddressString());
        log.info("┖ Channel: {}", connection.getProtocol().getChannel());
    }
}
