package kr.rtustudio.bridge.proxium.core.handler;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.internal.TransactionPacket;
import kr.rtustudio.bridge.proxium.core.ProxiumProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Velocity 프록시 측 공통 ConnectionHandler. 연결된 서버 추적, 채널 구독 관리, 패킷 중계 로직을 통합한다. */
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
        if (!proxium.isShuttingDown()) {
            logDisconnection(connection);
        }
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (!(packet instanceof byte[] frame)) return;

        BridgeChannel channel = proxium.peekChannel(frame);
        if (channel == null) return;

        // 프록시가 구독한 채널이면 디코딩하여 핸들러 실행 (예: PlayerEvent, TeleportRequest)
        Object decoded = proxium.dispatchPacket(frame);

        // 디코딩된 결과가 TransactionPacket이면 target 기반 릴레이
        if (decoded instanceof TransactionPacket txn) {
            proxium.relayTransactionPacket(connection, txn);
            return;
        }

        // 채널 구독 알림 처리
        if (decoded instanceof BridgeChannel subscribedChannel) {
            Set<BridgeChannel> subs = proxium.getServerSubscriptions().get(connection);
            if (subs != null) subs.add(subscribedChannel);
            return;
        }

        // 구독하지 않은 채널 (dispatchPacket이 null 반환)이지만
        // TransactionPacket일 수 있으므로 디코딩 시도하여 target 기반 릴레이
        if (decoded == null) {
            try {
                Object rawDecoded = proxium.getOptions().decode(frame);
                if (rawDecoded instanceof TransactionPacket txn) {
                    proxium.relayTransactionPacket(connection, txn);
                    return;
                }
            } catch (Exception ignored) {
                // 디코딩 실패 = 프록시가 알 필요 없는 패킷 → 구독 기반 릴레이로 폴백
            }
        }

        // 일반 pub/sub: 구독한 다른 서버에게 원본 frame 릴레이
        proxium.getConnectedServers().stream()
                .filter(conn -> !conn.equals(connection))
                .filter(
                        conn -> {
                            Set<BridgeChannel> subs = proxium.getServerSubscriptions().get(conn);
                            return subs != null && subs.contains(channel);
                        })
                .forEach(conn -> conn.send(frame));
    }

    private String resolveServerName(Connection connection) {
        ProxiumNode server = proxium.getProxiumNode(connection.getRemoteAddress()).orElse(null);
        return server != null ? server.name() : "Unknown";
    }

    private void logConnection(Connection connection) {
        synchronized (log) {
            log.info("Connected to {}", resolveServerName(connection));
            log.info("┠ Address: {}", connection.getRemoteAddressString());
            log.info("┖ Channel: {}", connection.getProtocol().getChannel());
        }
    }

    private void logDisconnection(Connection connection) {
        log.info("Disconnected from {}", resolveServerName(connection));
    }
}
