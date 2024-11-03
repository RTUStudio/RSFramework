package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.netty.Sender;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.protocol.internal.ProtocolRegister;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import lombok.Getter;

public class InternalConnectionHandler {

    @Getter
    protected static final Protocol protocol = Protocol.create("rslib", "protoweaver")
            .setServerHandler(ServerConnectionHandler.class)
            .setClientHandler(ClientConnectionHandler.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .addPacket(CustomPacket.class)
            .addPacket(ProtocolRegister.class)
            .load();

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!connection.getProtocol().toString().equals(protocol.toString())) return;
        if (sender != null) {
            sender.disconnect();
            return;
        }
        connection.disconnect();
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection) {
        disconnectIfNeverUpgraded(connection, null);
    }

    protected void protocolNotLoaded(ProtoConnection connection, String name) {
        ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing connection!");
        Sender sender = connection.send(new ProtocolStatus(connection.getProtocol().toString(), name, new byte[]{}, ProtocolStatus.Status.MISSING));
        disconnectIfNeverUpgraded(connection, sender);
    }
}