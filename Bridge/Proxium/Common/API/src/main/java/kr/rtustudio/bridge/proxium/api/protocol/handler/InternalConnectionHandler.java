package kr.rtustudio.bridge.proxium.api.protocol.handler;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.netty.Sender;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.status.AuthStatus;
import kr.rtustudio.bridge.proxium.api.protocol.status.ProtocolStatus;
import lombok.Getter;

public class InternalConnectionHandler {

    @Getter
    protected static final Protocol protocol =
            Protocol.create(BridgeChannel.PROXIUM)
                    .setServerHandler(ServerConnectionHandler.class)
                    .setProxyHandler(ProxyConnectionHandler.class)
                    .addPacket(AuthStatus.class)
                    .addPacket(ProtocolStatus.class)
                    .load();

    protected boolean wasUpgraded(Connection connection) {
        return connection.getProtocol().toString().equals(protocol.toString());
    }

    protected void disconnectIfNeverUpgraded(Connection connection, Sender sender) {
        if (!wasUpgraded(connection)) return;
        if (sender != null) {
            sender.disconnect();
            return;
        }
        connection.disconnect();
    }

    protected void disconnectIfNeverUpgraded(Connection connection) {
        disconnectIfNeverUpgraded(connection, null);
    }

    protected void protocolNotLoaded(Connection connection, String name) {
        // protocol missing state is sent to the remote side and handled there.
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
