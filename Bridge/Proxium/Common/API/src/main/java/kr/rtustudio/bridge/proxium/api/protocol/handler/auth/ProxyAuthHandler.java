package kr.rtustudio.bridge.proxium.api.protocol.handler.auth;

/**
 * A simple provider class for proxy authentication. Any implementations loaded on the server won't
 * do anything.
 */
public interface ProxyAuthHandler {

    /**
     * This function is called on the proxy when it needs to send its secret to the server for
     * authentication.
     *
     * @return The secret that will be sent to the server.
     */
    byte[] getSecret();
}
