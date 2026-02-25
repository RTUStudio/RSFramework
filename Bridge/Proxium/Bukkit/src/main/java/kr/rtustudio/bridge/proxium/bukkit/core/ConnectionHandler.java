package kr.rtustudio.bridge.proxium.bukkit.core;

import kr.rtustudio.bridge.proxium.api.netty.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Proxium")
@RequiredArgsConstructor
public class ConnectionHandler implements kr.rtustudio.bridge.proxium.api.ConnectionHandler {

    private static Connection proxy;
    private final BukkitProxium proxium;

    public static Connection getProxy() {
        if (proxy == null || !proxy.isOpen()) return null;
        return proxy;
    }

    @Override
    public void onReady(Connection connection) {
        String platform = proxium.getSecurity().isModernProxy() ? "Velocity" : "BungeeCord/Other";
        log.info("Connected to {}", platform);
        log.info("┠ Address: {}", connection.getRemoteAddressString());
        log.info("┖ Channel: {}", connection.getProtocol().getChannel());
        proxium.ready(connection);
        proxy = connection;
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (packet instanceof byte[] frame) {
            proxium.dispatchPacket(frame);
        }
    }
}
