package kr.rtustudio.bridge.proxium.api.protocol.handler;

import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.netty.Sender;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ServerAuthHandler;
import kr.rtustudio.bridge.proxium.api.protocol.status.AuthStatus;
import kr.rtustudio.bridge.proxium.api.protocol.status.ProtocolStatus;
import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j(topic = "Proxium")
public class ServerHandshakeHandler extends InternalConnectionHandler implements ConnectionHandler {

    private boolean authenticated = false;
    private Protocol nextProtocol = null;
    private ServerAuthHandler authHandler = null;

    @SneakyThrows
    @Override
    public void handlePacket(Connection connection, Object packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case START -> {
                    nextProtocol = ProxiumAPI.getLoadedProtocol(status.getNextProtocol());
                    if (nextProtocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }

                    if (!ProxiumConstants.PROXIUM_VERSION.equals(status.getVersion())) {
                        log.warn(
                                "[{}] Proxy connecting with version: {}, but server is running: {}. There could be unexpected issues.",
                                nextProtocol,
                                status.getVersion(),
                                ProxiumConstants.PROXIUM_VERSION);
                    }

                    if (nextProtocol.getMaxConnections() != -1
                            && nextProtocol.getConnections() >= nextProtocol.getMaxConnections()) {
                        status.setStatus(ProtocolStatus.Status.FULL);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    if (!Arrays.equals(nextProtocol.getSHA1(), status.getNextSHA1())) {
                        log.error(
                                "[{}] Mismatch with protocol version on the proxy! ({})",
                                nextProtocol,
                                nextProtocol.getChannel());
                        log.error(
                                "[{}] Double check that all packets are registered in the same order and all settings are the same.",
                                nextProtocol);

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
                    log.error("[{}] Protocol is not loaded on proxy!", nextProtocol);
                    disconnectIfNeverUpgraded(connection);
                }
                default -> {}
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
    }

    @Override
    public void onDisconnect(Connection connection) {
        if (wasUpgraded(connection)) {
            log.info("[{}] Disconnected from: {}", protocol, connection.getRemoteAddress());
        } else {
            log.debug(
                    "[{}] Disconnected from: {} (before upgrade)",
                    protocol,
                    connection.getRemoteAddress());
        }
    }
}
