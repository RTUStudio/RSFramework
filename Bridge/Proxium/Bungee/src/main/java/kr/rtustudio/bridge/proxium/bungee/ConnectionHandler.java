package kr.rtustudio.bridge.proxium.bungee;

import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.core.handler.ProxyConnectionHandler;

import java.net.SocketAddress;
import java.util.List;

public class ConnectionHandler extends ProxyConnectionHandler {

    public ConnectionHandler(BungeeProxium proxium) {
        super(proxium);
    }

    public static Connection getServer(SocketAddress address) {
        return ProxyConnectionHandler.getServer(address);
    }

    public static List<Connection> getServers() {
        return ProxyConnectionHandler.getServers();
    }
}
