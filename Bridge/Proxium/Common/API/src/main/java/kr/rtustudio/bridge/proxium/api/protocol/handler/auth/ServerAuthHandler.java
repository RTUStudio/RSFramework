package kr.rtustudio.bridge.proxium.api.protocol.handler.auth;

import kr.rtustudio.bridge.proxium.api.netty.Connection;

/**
 * A simple provider class for server authentication. Any implementations loaded on the proxy won't
 * do anything.
 */
public interface ServerAuthHandler {

    /**
     * This function is called on the server when a proxy secret is received.
     *
     * @param connection The current connection.
     * @param secret The secret sent from the proxy
     * @return True to accept the connection, false to block it.
     */
    boolean handleAuth(Connection connection, byte[] secret);
}
