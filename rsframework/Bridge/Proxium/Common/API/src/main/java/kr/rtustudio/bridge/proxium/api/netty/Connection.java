package kr.rtustudio.bridge.proxium.api.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a connection to either a proxy or a server.
 *
 * <p>프록시 또는 서버와의 연결을 나타내는 클래스.
 */
@Slf4j
public class Connection {

    private static final ConcurrentHashMap<String, Integer> connectionCount =
            new ConcurrentHashMap<>();

    private final PacketHandler packetHandler;
    private final Channel channel;
    private final ChannelPipeline pipeline;

    /**
     * Gets the side that this connection is on. Always returns {@link Side#PROXY} on proxy and
     * {@link Side#SERVER} on server.
     *
     * <p>이 연결의 컡을 반환한다. 프록시에서는 항상 {@link Side#PROXY}, 서버에서는 {@link Side#SERVER}를 반환한다.
     */
    @Getter private final Side side;

    @Getter private ConnectionHandler handler;

    /**
     * Gets the current protocol of this connection.
     *
     * <p>연결의 현재 프로토콜을 반환한다.
     */
    @Getter private Protocol protocol;

    /**
     * Gets which side closed the connection.
     *
     * <p>연결을 닫은 컡을 반환한다.
     */
    @Getter private Side disconnecter;

    public Connection(@NonNull Protocol protocol, @NonNull Side side, @NonNull Channel channel) {
        this.side = side;
        this.disconnecter = Side.SERVER == side ? Side.PROXY : Side.SERVER;
        this.protocol = protocol;
        this.handler = protocol.newConnectionHandler(side);
        this.packetHandler = new PacketHandler(this, connectionCount);
        packetHandler.setHandler(handler);
        this.channel = channel;
        this.pipeline = channel.pipeline();

        pipeline.addLast("packetHandler", packetHandler);
        setCompression(protocol);
    }

    /**
     * Returns the number of currently connected proxies for this protocol.
     *
     * <p>이 프로토콜에 현재 연결된 프록시 수를 반환한다.
     *
     * @return connection count for the protocol
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
     * Changes the current protocol to the given one. Must be called on both proxy and server to
     * avoid protocol mismatch.
     *
     * <p>현재 커넥션 프로토콜을 변경한다. 양쪽(proxy, server) 모두에서 호출해야 한다.
     *
     * @param protocol the protocol to switch to
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
            log.error("[{}] Threw an error on initialization!", protocol, e);
        }
    }

    /**
     * Checks if the connection is open.
     *
     * <p>연결이 열려 있는지 확인한다.
     *
     * @return {@code true} if open, {@code false} if closed
     */
    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * Returns the remote address of the connection.
     *
     * <p>연결의 원격 주소를 반환한다.
     *
     * @return {@link InetSocketAddress}
     */
    public InetSocketAddress getRemoteAddress() {
        return ((InetSocketAddress) channel.remoteAddress());
    }

    /**
     * Returns the remote address as a formatted string.
     *
     * <p>원격 주소를 포맷된 문자열로 반환한다.
     *
     * @return remote address in {@code "host:port"} format
     */
    public String getRemoteAddressString() {
        InetSocketAddress addr = getRemoteAddress();
        if (addr == null) return "unknown";
        String host =
                (addr.getAddress() != null)
                        ? addr.getAddress().getHostAddress()
                        : addr.getHostString();
        if ("localhost".equalsIgnoreCase(host)) {
            host = "127.0.0.1";
        }
        return host + ":" + addr.getPort();
    }

    /**
     * Sends a packet to the connected peer.
     *
     * <p>연결된 피어에 패킷을 전송한다.
     *
     * @return a {@link Sender} that can close the connection after sending
     */
    public Sender send(@NonNull Object packet) {
        return packetHandler.send(packet);
    }

    /**
     * Closes the connection if open. No-op on an already closed connection.
     *
     * <p>열려 있으면 연결을 닫는다. 이미 닫힌 연결에서는 아무 동작도 하지 않는다.
     */
    public void disconnect() {
        if (isOpen()) channel.close();
        disconnecter = side;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Connection connection)) return false;
        return protocol.equals(connection.getProtocol())
                && Objects.equals(getRemoteAddress(), connection.getRemoteAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, getRemoteAddress());
    }

    @Override
    public String toString() {
        return "[" + protocol.toString() + ", " + getRemoteAddress() + "]";
    }
}
