package kr.rtustudio.protoweaver.bungee.core;

import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.protoweaver.api.netty.ProtoConnection;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

@Slf4j(topic = "RSF/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class BungeeProtoHandler implements ProtoConnectionHandler {

    private static final List<ProtoConnection> servers = new ArrayList<>();
    private final HandlerCallback callable;

    public static ProtoConnection getServer(SocketAddress address) {
        for (ProtoConnection server : getServers()) {
            if (server.getRemoteAddress().equals(address)) return server;
        }
        return null;
    }

    public static List<ProtoConnection> getServers() {
        List<ProtoConnection> result = new ArrayList<>();
        for (ProtoConnection server : ImmutableList.copyOf(servers)) {
            if (server.isOpen()) result.add(server);
            else servers.remove(server);
        }
        return result;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        servers.add(protoConnection);
        log.info("Connected to Server");
        log.info("┠ Address: {}", protoConnection.getRemoteAddress());
        log.info("┖ Protocol: {}", protoConnection.getProtocol().getNamespaceKey());
        if (callable != null) callable.onReady(protoConnection);
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
        if (protoConnection.getProtocol().isGlobal(packet))
            getServers().forEach(connection -> connection.send(packet));
    }
}
