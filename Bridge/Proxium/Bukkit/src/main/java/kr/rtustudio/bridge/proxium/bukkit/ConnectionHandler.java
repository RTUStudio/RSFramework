package kr.rtustudio.bridge.proxium.bukkit;

import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.internal.TransactionPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Proxium")
@RequiredArgsConstructor
public class ConnectionHandler
        implements kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler {

    private final BukkitProxium proxium;

    @Override
    public void onReady(Connection connection) {
        synchronized (log) {
            log.info("Connected to velocity");
            log.info("┠ Address: {}", connection.getRemoteAddressString());
            log.info("┖ Channel: {}", connection.getProtocol().getChannel());
        }
        proxium.ready(connection);
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (packet instanceof byte[] frame) {
            Object decoded = proxium.dispatchPacket(frame);
            // RPC 패킷은 어떤 채널에서든 올 수 있으므로 TransactionPacket 감지 후 처리
            if (decoded instanceof TransactionPacket txn) {
                proxium.handleBridgePacket(txn);
            } else if (decoded == null) {
                // 구독하지 않은 채널이지만 TransactionPacket일 수 있음
                try {
                    Object rawDecoded = proxium.getOptions().decode(frame);
                    if (rawDecoded instanceof TransactionPacket txn) {
                        proxium.handleBridgePacket(txn);
                    }
                } catch (Exception ignored) {
                    // 알 수 없는 패킷 → 무시
                }
            }
        }
    }

    @Override
    public void onDisconnect(Connection connection) {
        log.info("Disconnected from velocity. Operating in standalone mode.");
        proxium.setNode(null);
    }
}
