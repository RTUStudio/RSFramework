package kr.rtustudio.bridge.proxium.api.protocol.handler;

import kr.rtustudio.bridge.proxium.api.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ProxyAuthHandler;
import kr.rtustudio.bridge.proxium.api.protocol.status.AuthStatus;
import kr.rtustudio.bridge.proxium.api.protocol.status.ProtocolStatus;
import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;

public class ProxyConnectionHandler extends InternalConnectionHandler implements ConnectionHandler {

    private Protocol protocol;
    private boolean authenticated = false;
    private ProxyAuthHandler authHandler = null;

    public void start(Connection connection, Protocol protocol) {
        this.protocol = protocol;
        authenticated = false;
        if (protocol.requiresAuth(Side.PROXY)) authHandler = protocol.newProxyAuthHandler();
        connection.send(
                new ProtocolStatus(
                        connection.getProtocol().toString(),
                        protocol.toString(),
                        protocol.getSHA1(),
                        ProtocolStatus.Status.START));
    }

    @Override
    public void handlePacket(Connection connection, Object packet) {
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
                    if (!ProxiumConstants.PROXIUM_VERSION.equals(status.getVersion())) {
                        protocol.logWarn(
                                "Connecting with ProxiumAPI version: "
                                        + status.getVersion()
                                        + ", but server is running: "
                                        + ProxiumConstants.PROXIUM_VERSION
                                        + ". There could be unexpected issues");
                    }

                    if (!authenticated) return;
                    protocol = ProxiumAPI.getLoadedProtocol(status.getNextProtocol());
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
                case OK -> authenticated = true;
                case REQUIRED -> {
                    if (authHandler == null) {
                        protocol.logErr(
                                "Proxy protocol has not defined an auth handler, but the server at: "
                                        + connection.getRemoteAddress()
                                        + " requires auth. Closing connection");
                        connection.disconnect();
                        return;
                    }
                    connection.send(authHandler.getSecret());
                }
                case DENIED -> {
                    protocol.logErr("Denied access by server at: " + connection.getRemoteAddress());
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }
    }

    @Override
    public void onDisconnect(Connection connection) {
        if (wasUpgraded(connection))
            protocol.logInfo("Disconnected from: " + connection.getRemoteAddress());
    }
}
