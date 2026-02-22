package kr.rtustudio.bridge.protoweaver.core.protocol.protoweaver;

import kr.rtustudio.bridge.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.bridge.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.protocol.Protocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

@Slf4j(topic = "RSF/ProtoHandler")
@RequiredArgsConstructor
public class ServerPacketHandler implements ProtoConnectionHandler {

    private static final List<ProtoConnection> servers = new ArrayList<>();
    private final HandlerCallback callable;

    public ServerPacketHandler() {
        this.callable = null;
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
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
        Protocol protocol = protoConnection.getProtocol();
        if (protocol.isGlobal(packet)) {
            getServers()
                    .forEach(
                            connection -> {
                                if (protocol.equals(connection.getProtocol()))
                                    connection.send(packet);
                            });
        }
    }
}
