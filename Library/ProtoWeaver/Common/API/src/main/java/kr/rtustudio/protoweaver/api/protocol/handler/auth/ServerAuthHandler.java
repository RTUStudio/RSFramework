package kr.rtustudio.protoweaver.api.protocol.handler.auth;

import kr.rtustudio.protoweaver.api.netty.ProtoConnection;

/**
 * A simple provider class for server authentication. Any implementations loaded on the client won't
 * do anything.
 */
public interface ServerAuthHandler {

    /**
     * This function is called on the server when a client secret is received.
     *
     * @param connection The current connection.
     * @param secret The secret sent from the client
     * @return True to accept the connection, false to block it.
     */
    boolean handleAuth(ProtoConnection connection, byte[] secret);
}
