package kr.rtustudio.bridge.proxium.velocity;

import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.proxy.RegisteredServer;
import kr.rtustudio.bridge.proxium.core.handler.ProxyConnectionHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

@Slf4j(topic = "Proxium")
public class ConnectionHandler extends ProxyConnectionHandler {

    private final VelocityProxium velocityProxium;

    public ConnectionHandler(VelocityProxium proxium) {
        super(proxium);
        this.velocityProxium = proxium;
    }

    public static Connection getServer(SocketAddress address) {
        return ProxyConnectionHandler.getServer(address);
    }

    public static List<Connection> getServers() {
        return ProxyConnectionHandler.getServers();
    }

    @Override
    protected void logConnection(Connection connection) {
        String name = "Unknown Server";
        InetSocketAddress address = connection.getRemoteAddress();
        RegisteredServer server = velocityProxium.getServer(address);
        if (server != null) name = server.name();

        log.info("Connected to {}", name);
        log.info("┠ Address: {}", connection.getRemoteAddressString());
        log.info("┖ Channel: {}", connection.getProtocol().getChannel());
    }
}
