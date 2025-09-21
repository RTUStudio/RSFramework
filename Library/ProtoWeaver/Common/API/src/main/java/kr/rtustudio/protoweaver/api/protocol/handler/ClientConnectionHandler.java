package kr.rtustudio.protoweaver.api.protocol.handler;

import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.ProtoWeaver;
import kr.rtustudio.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.protoweaver.api.protocol.Protocol;
import kr.rtustudio.protoweaver.api.protocol.Side;
import kr.rtustudio.protoweaver.api.protocol.handler.auth.ClientAuthHandler;
import kr.rtustudio.protoweaver.api.protocol.status.AuthStatus;
import kr.rtustudio.protoweaver.api.protocol.status.ProtocolStatus;
import kr.rtustudio.protoweaver.api.util.ProtoConstants;

public class ClientConnectionHandler extends InternalConnectionHandler
        implements ProtoConnectionHandler {

    private Protocol protocol;
    private boolean authenticated = false;
    private ClientAuthHandler authHandler = null;

    public void start(ProtoConnection connection, Protocol protocol) {
        this.protocol = protocol;
        authenticated = false;
        if (protocol.requiresAuth(Side.CLIENT)) authHandler = protocol.newClientAuthHandler();
        connection.send(
                new ProtocolStatus(
                        connection.getProtocol().toString(),
                        protocol.toString(),
                        protocol.getSHA1(),
                        ProtocolStatus.Status.START));
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case MISSING -> {
                    protocol.logErr("Not loaded on server: " + connection.getRemoteAddress());
                    // disconnectIfNeverUpgraded(connection);
                }
                case MISMATCH -> {
                    protocol.logErr(
                            "Mismatch with protocol version on server: "
                                    + connection.getRemoteAddress());
                    protocol.logErr(
                            "Double check that all packets are registered in the same order and all settings are the same");
                    disconnectIfNeverUpgraded(connection);
                }
                case FULL -> {
                    protocol.logErr(
                            "The maximum number of allowed connections on server: "
                                    + connection.getRemoteAddress()
                                    + " has been reached!");
                    disconnectIfNeverUpgraded(connection);
                }
                case UPGRADE -> {
                    if (!ProtoConstants.PROTOWEAVER_VERSION.equals(status.getVersion())) {
                        protocol.logWarn(
                                "Connecting with ProtoWeaver version: "
                                        + status.getVersion()
                                        + ", but server is running: "
                                        + ProtoConstants.PROTOWEAVER_VERSION
                                        + ". There could be unexpected issues");
                    }

                    if (!authenticated) return;
                    protocol = ProtoWeaver.getLoadedProtocol(status.getNextProtocol());
                    if (protocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }
                    connection.upgradeProtocol(protocol);
                    // protocol.logInfo("Connected to: " + connection.getRemoteAddress());
                }
            }
            return;
        }

        if (packet instanceof AuthStatus auth) {
            switch (auth) {
                case AuthStatus.OK -> authenticated = true;
                case AuthStatus.REQUIRED -> {
                    if (authHandler == null) {
                        protocol.logErr(
                                "Client protocol has not defined an auth handler, but the server at: "
                                        + connection.getRemoteAddress()
                                        + " requires auth. Closing connection");
                        connection.disconnect();
                        return;
                    }
                    connection.send(authHandler.getSecret());
                }
                case AuthStatus.DENIED -> {
                    protocol.logErr("Denied access by server at: " + connection.getRemoteAddress());
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
        if (wasUpgraded(connection))
            protocol.logInfo("Disconnected from: " + connection.getRemoteAddress());
    }
}
