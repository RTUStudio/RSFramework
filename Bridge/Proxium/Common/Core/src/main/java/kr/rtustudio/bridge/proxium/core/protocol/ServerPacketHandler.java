package kr.rtustudio.bridge.proxium.core.protocol;

import kr.rtustudio.bridge.proxium.api.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j(topic = "Proxium")
@RequiredArgsConstructor
public class ServerPacketHandler implements ConnectionHandler {

    private static final CopyOnWriteArrayList<Connection> servers = new CopyOnWriteArrayList<>();

    public static List<Connection> getServers() {
        servers.removeIf(server -> !server.isOpen());
        return List.copyOf(servers);
    }

    @Override
    public void onReady(Connection connection) {
        servers.add(connection);
        log.info("Connected to Server");
        log.info("┠ Address: {}", connection.getRemoteAddressString());
        log.info("┖ Protocol: {}", connection.getProtocol().getChannel());
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {}
}
