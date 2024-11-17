package kr.rtuserver.protoweaver.api.protocol.protomessage;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.CompressionType;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import kr.rtuserver.protoweaver.api.util.Event;
import lombok.Getter;

/**
 * Serves mostly as an example protocol, however it can be used in your project if you desire.
 */
public class ProtoMessage implements ProtoConnectionHandler {

    /**
     * This event is triggered when a message is received and can be used both on the server and the client.
     * Be sure to load this protocol.
     */
    public static final Event<MessageReceived> MESSAGE_RECEIVED = new Event<>(callbacks -> (connection, channel, message) -> {
        callbacks.forEach(callback -> callback.trigger(connection, channel, message));
    });
    @Getter
    private static final Protocol protocol = Protocol.create("protoweaver", "proto-message")
            .setCompression(CompressionType.SNAPPY)
            .setServerHandler(ProtoMessage.class)
            .setClientHandler(ProtoMessage.class)
            .addPacket(Message.class)
            .build();

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        if (packet instanceof Message message)
            MESSAGE_RECEIVED.getInvoker().trigger(connection, message.getChannel(), message.getMessage());
    }

    @FunctionalInterface
    public interface MessageReceived {
        void trigger(ProtoConnection connection, String channel, String message);
    }
}