package kr.rtustudio.bridge.proxium.core.handler;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.core.ProxiumProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Bungee/Velocity 프록시 측 공통 ConnectionHandler. 연결된 서버 추적, 채널 구독 관리, 패킷 중계 로직을 통합한다. */
@Slf4j(topic = "Proxium")
public class ProxyConnectionHandler implements ConnectionHandler {

    protected final ProxiumProxy proxium;

    public ProxyConnectionHandler(ProxiumProxy proxium) {
        this.proxium = proxium;
    }

    @Override
    public void onReady(Connection connection) {
        proxium.getServerSubscriptions().put(connection, ConcurrentHashMap.newKeySet());
        proxium.ready(connection);
        logConnection(connection);
    }

    @Override
    public void onDisconnect(Connection connection) {
        proxium.getServerSubscriptions().remove(connection);
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (!(packet instanceof byte[] frame)) return;

        BridgeChannel channel = proxium.peekChannel(frame);
        if (channel == null) return;

        Object decoded = proxium.dispatchPacket(frame);

        if (decoded instanceof BridgeChannel subscribedChannel) {
            Set<BridgeChannel> subs = proxium.getServerSubscriptions().get(connection);
            if (subs != null) subs.add(subscribedChannel);
            return;
        }

        proxium.getConnectedServers().stream()
                .filter(conn -> !conn.equals(connection))
                .filter(
                        conn -> {
                            Set<BridgeChannel> subs = proxium.getServerSubscriptions().get(conn);
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
