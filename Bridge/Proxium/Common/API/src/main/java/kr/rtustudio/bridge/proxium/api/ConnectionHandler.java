package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.proxium.api.netty.Connection;

/** A packets handler for your custom protocol. */
public interface ConnectionHandler {
    /**
     * This function is called once the connection is ready to start sending/receiving packets.
     *
     * @param connection The current connection.
     */
    default void onReady(Connection connection) {}

    /**
     * This function is called when the connection is closed.
     *
     * @param connection The closed connection.
     */
    default void onDisconnect(Connection connection) {}

    /**
     * This function is called everytime a packets is received on your protocol.
     *
     * @param connection The current connection.
     * @param packet The received object. use "instanceof" to check which one of your packets it is.
     */
    default void handlePacket(Connection connection, Object packet) {}
}
