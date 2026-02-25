package kr.rtustudio.bridge.proxium.api.protocol.handler;

import kr.rtustudio.bridge.proxium.api.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.netty.Sender;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ServerAuthHandler;
import kr.rtustudio.bridge.proxium.api.protocol.status.AuthStatus;
import kr.rtustudio.bridge.proxium.api.protocol.status.ProtocolStatus;
import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;
import lombok.SneakyThrows;

import java.util.Arrays;

public class ServerConnectionHandler extends InternalConnectionHandler
        implements ConnectionHandler {

    private boolean authenticated = false;
    private Protocol nextProtocol = null;
    private ServerAuthHandler authHandler = null;

    @SneakyThrows
    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case START -> {
                    // Check if protocol loaded
                    nextProtocol = ProxiumAPI.getLoadedProtocol(status.getNextProtocol());
                    if (nextProtocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }

                    if (!ProxiumConstants.PROXIUM_VERSION.equals(status.getVersion())) {
                        nextProtocol.logWarn(
                                "Proxy connecting with ProxiumAPI version: "
                                        + status.getVersion()
                                        + ", but server is running: "
                                        + ProxiumConstants.PROXIUM_VERSION
                                        + ". There could be unexpected issues.");
                    }

                    if (nextProtocol.getMaxConnections() != -1
                            && nextProtocol.getConnections() >= nextProtocol.getMaxConnections()) {
                        status.setStatus(ProtocolStatus.Status.FULL);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    if (!Arrays.equals(nextProtocol.getSHA1(), status.getNextSHA1())) {
                        nextProtocol.logErr(
                                "Mismatch with protocol version on the proxy! ("
                                        + nextProtocol.getChannel()
                                        + ")");
                        nextProtocol.logErr(
                                "Double check that all packets are registered in the same order and all settings are the same.");

                        status.setStatus(ProtocolStatus.Status.MISMATCH);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    if (nextProtocol.requiresAuth(Side.SERVER)) {
                        authHandler = nextProtocol.newServerAuthHandler();
                        connection.send(AuthStatus.REQUIRED);
                        return;
                    }

                    authenticated = true;
                }
                case MISSING -> {
                    nextProtocol.logErr("Protocol is not loaded on proxy!");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }

        // Authenticate proxy
        if (nextProtocol != null && packet instanceof byte[] secret) {
            authenticated = authHandler.handleAuth(connection, secret);
        }

        if (!authenticated) {
            Sender sender = connection.send(AuthStatus.DENIED);
            disconnectIfNeverUpgraded(connection, sender);
            return;
        }

        // Upgrade protocol
        connection.send(AuthStatus.OK);
        connection.send(
                new ProtocolStatus(
                        connection.getProtocol().toString(),
                        nextProtocol.toString(),
                        new byte[] {},
                        ProtocolStatus.Status.UPGRADE));
        connection.upgradeProtocol(nextProtocol);
        // nextProtocol.logInfo("Connected to: " + connection.getRemoteAddress());
    }

    @Override
    public void onDisconnect(Connection connection) {
        protocol.logInfo("Disconnected from: " + connection.getRemoteAddress());
    }
}
