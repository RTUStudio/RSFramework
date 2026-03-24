package kr.rtustudio.bridge.proxium.bukkit;

import kr.rtustudio.bridge.proxium.api.netty.Connection;
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
            proxium.dispatchPacket(frame);
        }
    }

    @Override
    public void onDisconnect(Connection connection) {
        log.info("Disconnected from velocity. Operating in standalone mode.");
        proxium.setNode(null);
    }
}
