package kr.rtustudio.bridge.proxium.velocity;

import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.core.handler.ProxyConnectionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Proxium")
public class ConnectionHandler extends ProxyConnectionHandler {

    private final VelocityProxium velocityProxium;

    public ConnectionHandler(VelocityProxium proxium) {
        super(proxium);
        this.velocityProxium = proxium;
    }

    @Override
    protected void logConnection(Connection connection) {
        ProxiumNode server =
                velocityProxium.getProxiumNode(connection.getRemoteAddress()).orElse(null);
        String name = server != null ? server.name() : "Unknown";
        log.info("Connected to {}", name);
        log.info("┠ Address: {}", connection.getRemoteAddressString());
        log.info("┖ Channel: {}", connection.getProtocol().getChannel());
    }

    @Override
    protected void logDisconnection(Connection connection) {
        ProxiumNode server =
                velocityProxium.getProxiumNode(connection.getRemoteAddress()).orElse(null);
        String name = server != null ? server.name() : "Unknown";
        log.warn("Disconnected from {}", name);
    }
}
