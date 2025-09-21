package kr.rtustudio.protoweaver.api.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.protocol.CompressionType;
import kr.rtustudio.protoweaver.api.protocol.Protocol;
import kr.rtustudio.protoweaver.api.protocol.Side;
import kr.rtustudio.protoweaver.api.protocol.internal.InternalPacket;
import lombok.Getter;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** This provider represents a connection to either a client or a server */
public class ProtoConnection {

    private static final ConcurrentHashMap<String, Integer> connectionCount =
            new ConcurrentHashMap<>();

    private final ProtoPacketHandler packetHandler;
    private final Channel channel;
    private final ChannelPipeline pipeline;

    /**
     * Get the side that this connection is on. Always returns {@link Side#CLIENT} on client and
     * {@link Side#SERVER} on server.
     */
    @Getter private final Side side;

    @Getter private ProtoConnectionHandler handler;

    /** Get the connections current protocol. */
    @Getter private Protocol protocol;

    /** Get which side closed the connection. */
    @Getter private Side disconnecter;

    public ProtoConnection(
            @NonNull Protocol protocol, @NonNull Side side, @NonNull Channel channel) {
        this.side = side;
        this.disconnecter = Side.SERVER == side ? Side.CLIENT : Side.SERVER;
        this.protocol = protocol;
        this.handler = protocol.newConnectionHandler(side);
        this.packetHandler = new ProtoPacketHandler(this, connectionCount);
        packetHandler.setHandler(handler);
        this.channel = channel;
        this.pipeline = channel.pipeline();

        pipeline.addLast("packetHandler", packetHandler);
        setCompression(protocol);
    }

    /**
     * @return The number of connected clients the passed in protocol is currently serving.
     */
    public static int getConnectionCount(Protocol protocol) {
        return connectionCount.getOrDefault(protocol.toString(), 0);
    }

    private void setCompression(@NonNull Protocol protocol) {
        CompressionType compression = this.protocol.getCompression();
        if (protocol.getCompression().equals(compression)) return;

        if (pipeline.names().contains("compressionEncoder")) {
            pipeline.remove("compressionEncoder");
            pipeline.remove("compressionDecoder");
        }

        int level = protocol.getCompressionLevel();
        switch (protocol.getCompression()) {
            case GZIP -> {
                pipeline.addBefore(
                        "packetHandler",
                        "compressionEncoder",
                        ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, level));
                pipeline.addAfter(
                        "compressionEncoder",
                        "compressionDecoder",
                        ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
            }
            case SNAPPY -> {
                pipeline.addBefore("packetHandler", "compressionEncoder", new SnappyFrameEncoder());
                pipeline.addAfter(
                        "compressionEncoder", "compressionDecoder", new SnappyFrameDecoder());
            }
            case FAST_LZ -> {
                pipeline.addBefore(
                        "packetHandler", "compressionEncoder", new FastLzFrameEncoder(level));
                pipeline.addAfter(
                        "compressionEncoder", "compressionDecoder", new FastLzFrameDecoder());
            }
        }
    }

    /**
     * Changes the current connection protocol to the given protocol. NOTE: You must call this on
     * both the client and server or else you will have a protocol mismatch.
     *
     * @param protocol The protocol the connection will switch to.
     */
    public void upgradeProtocol(@NonNull Protocol protocol) {
        try {
            setCompression(protocol);
            this.handler = protocol.newConnectionHandler(side);
            connectionCount.put(
                    protocol.toString(), connectionCount.getOrDefault(protocol.toString(), 1) - 1);
            this.protocol = protocol;
            connectionCount.put(
                    protocol.toString(), connectionCount.getOrDefault(protocol.toString(), 0) + 1);
            packetHandler.setHandler(handler);

            this.handler.onReady(this);
        } catch (Exception e) {
            protocol.logErr("Threw an error on initialization!");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the connection is open.
     *
     * @return True if open, false if closed.
     */
    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * Get the remote address of the connection. Check {@link InetSocketAddress} for more
     * information.
     *
     * @return {@link InetSocketAddress}
     */
    public InetSocketAddress getRemoteAddress() {
        return ((InetSocketAddress) channel.remoteAddress());
    }

    /**
     * Sends a {@link InternalPacket} to the connected peer.
     *
     * @return A {@link Sender} that can be used to close the connection after the packets is sent.
     */
    public Sender send(@NonNull Object packet) {
        return packetHandler.send(packet);
    }

    /**
     * Closes the connection if it is open. Calling this function on a closed connection does
     * nothing.
     */
    public void disconnect() {
        if (isOpen()) channel.close();
        disconnecter = side;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProtoConnection connection)) return false;
        return protocol.equals(connection.getProtocol())
                && Objects.equals(getRemoteAddress(), connection.getRemoteAddress());
    }

    @Override
    public String toString() {
        return "[" + protocol.toString() + ", " + getRemoteAddress() + "]";
    }
}
