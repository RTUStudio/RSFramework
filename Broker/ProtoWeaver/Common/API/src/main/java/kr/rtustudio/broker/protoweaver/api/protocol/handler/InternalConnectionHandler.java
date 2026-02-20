package kr.rtustudio.broker.protoweaver.api.protocol.handler;

import kr.rtustudio.broker.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.broker.protoweaver.api.netty.Sender;
import kr.rtustudio.broker.protoweaver.api.protocol.Protocol;
import kr.rtustudio.broker.protoweaver.api.protocol.status.AuthStatus;
import kr.rtustudio.broker.protoweaver.api.protocol.status.ProtocolStatus;
import lombok.Getter;

public class InternalConnectionHandler {

    @Getter
    protected static final Protocol protocol =
            Protocol.create("rsframework", "protoweaver")
                    .setServerHandler(ServerConnectionHandler.class)
                    .setClientHandler(ClientConnectionHandler.class)
                    .addPacket(AuthStatus.class)
                    .addPacket(ProtocolStatus.class)
                    .load();

    protected boolean wasUpgraded(ProtoConnection connection) {
        return connection.getProtocol().toString().equals(protocol.toString());
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!wasUpgraded(connection)) return;
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
        // ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing
        // connection!");
        Sender sender =
                connection.send(
                        new ProtocolStatus(
                                connection.getProtocol().toString(),
                                name,
                                new byte[] {},
                                ProtocolStatus.Status.MISSING));
        // disconnectIfNeverUpgraded(connection, sender);
    }
}
