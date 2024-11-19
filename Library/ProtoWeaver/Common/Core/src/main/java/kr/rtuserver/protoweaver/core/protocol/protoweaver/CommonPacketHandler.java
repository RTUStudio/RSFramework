package kr.rtuserver.protoweaver.core.protocol.protoweaver;

import com.google.common.collect.ImmutableList;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "RSF/ProtoHandler")
@RequiredArgsConstructor
public class CommonPacketHandler implements ProtoConnectionHandler {

    private static final List<ProtoConnection> servers = new ArrayList<>();
    private final HandlerCallback callable;

    public CommonPacketHandler() {
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
            getServers().forEach(connection -> {
                if (protocol.equals(connection.getProtocol())) connection.send(packet);
            });
        }
    }
}