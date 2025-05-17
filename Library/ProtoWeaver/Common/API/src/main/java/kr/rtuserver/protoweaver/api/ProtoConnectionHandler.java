package kr.rtuserver.protoweaver.api;

import kr.rtuserver.protoweaver.api.netty.ProtoConnection;

/**
 * A packets handler for your custom protocol.
 */
public interface ProtoConnectionHandler {
    /**
     * This function is called once the connection is ready to start sending/receiving packets.
     *
     * @param connection The current connection.
     */
    default void onReady(ProtoConnection connection) {
    }

    /**
     * This function is called when the connection is closed.
     *
     * @param connection The closed connection.
     */
    default void onDisconnect(ProtoConnection connection) {
    }

    /**
     * This function is called everytime a packets is received on your protocol.
     *
     * @param connection The current connection.
     * @param packet     The received object. use "instanceof" to check which one of your packets it is.
     */
    default void handlePacket(ProtoConnection connection, Object packet) {
    }

    ;
}