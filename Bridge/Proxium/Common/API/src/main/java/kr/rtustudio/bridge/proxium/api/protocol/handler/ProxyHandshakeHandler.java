package kr.rtustudio.bridge.proxium.api.protocol.handler;

import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ProxyAuthHandler;
import kr.rtustudio.bridge.proxium.api.protocol.status.AuthStatus;
import kr.rtustudio.bridge.proxium.api.protocol.status.ProtocolStatus;
import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Proxium")
public class ProxyHandshakeHandler extends InternalConnectionHandler implements ConnectionHandler {

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
                case MISSING ->
                        log.error(
                                "[{}] Not loaded on server: {}",
                                protocol,
                                connection.getRemoteAddress());
                case MISMATCH -> {
                    log.error(
                            "[{}] Mismatch with protocol version on server: {}",
                            protocol,
                            connection.getRemoteAddress());
                    log.error(
                            "[{}] Double check that all packets are registered in the same order and all settings are the same",
                            protocol);
                    disconnectIfNeverUpgraded(connection);
                }
                case FULL -> {
                    log.error(
                            "[{}] The maximum number of allowed connections on server: {} has been reached!",
                            protocol,
                            connection.getRemoteAddress());
                    disconnectIfNeverUpgraded(connection);
                }
                case UPGRADE -> {
                    if (!ProxiumConstants.PROXIUM_VERSION.equals(status.getVersion())) {
                        log.warn(
                                "[{}] Connecting with version: {}, but server is running: {}. There could be unexpected issues",
                                protocol,
                                status.getVersion(),
                                ProxiumConstants.PROXIUM_VERSION);
                    }

                    if (!authenticated) return;
                    protocol = ProxiumAPI.getLoadedProtocol(status.getNextProtocol());
                    if (protocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }
                    connection.upgradeProtocol(protocol);
                }
                default -> {}
            }
            return;
        }

        if (packet instanceof AuthStatus auth) {
            switch (auth) {
                case OK -> authenticated = true;
                case REQUIRED -> {
                    if (authHandler == null) {
                        log.error(
                                "[{}] Proxy protocol has not defined an auth handler, but the server at: {} requires auth. Closing connection",
                                protocol,
                                connection.getRemoteAddress());
                        connection.disconnect();
                        return;
                    }
                    connection.send(authHandler.getSecret());
                }
                case DENIED -> {
                    log.error(
                            "[{}] Denied access by server at: {}",
                            protocol,
                            connection.getRemoteAddress());
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }
    }

    @Override
    public void onDisconnect(Connection connection) {
        if (protocol != null && wasUpgraded(connection))
            log.info("[{}] Disconnected from: {}", protocol, connection.getRemoteAddress());
    }
}
