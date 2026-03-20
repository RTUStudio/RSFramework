package kr.rtustudio.bridge.proxium.api.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.util.DrunkenBishop;
import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "Proxium")
public class PacketHandler extends ByteToMessageDecoder {

    private static ConcurrentHashMap<String, Integer> connectionCount;

    private final Connection connection;
    @Setter private ConnectionHandler handler;
    private ChannelHandlerContext ctx;
    private ByteBuf buf = Unpooled.buffer();

    public PacketHandler(
            Connection connection, ConcurrentHashMap<String, Integer> connectionCount) {
        this.connection = connection;
        PacketHandler.connectionCount = connectionCount;
        if (connection.getSide().isProxy()) {
            buf.writeByte(0); // Fake out minecraft packet len
            buf.writeByte(ProxiumConstants.PROXIUM_MAGIC_BYTE);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            connectionCount.put(
                    connection.getProtocol().toString(),
                    connectionCount.getOrDefault(connection.getProtocol().toString(), 1) - 1);
            this.handler.onDisconnect(connection);
        } catch (Exception e) {
            log.error("[{}] Threw an error on disconnect!", connection.getProtocol(), e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        // Ensure the whole packet has arrived before trying to decode
        if (byteBuf.readableBytes() < 4) return;
        byteBuf.markReaderIndex();
        int packetLen = byteBuf.readInt();
        if (byteBuf.readableBytes() < packetLen) {
            byteBuf.resetReaderIndex();
            return;
        }

        BridgeOptions options = connection.getProtocol().getOptions();
        Object packet = null;
        try {
            byte[] bytes = new byte[packetLen];
            byteBuf.readBytes(bytes);
            packet = options.deserializeRaw(bytes);
            handler.handlePacket(connection, packet);

        } catch (IllegalArgumentException e) {
            log.warn("[{}] Ignoring an {}", connection.getProtocol(), e.getMessage());
        } catch (Exception e) {
            if (packet != null)
                log.error(
                        "[{}] Threw an error when trying to handle: {}!",
                        connection.getProtocol(),
                        packet.getClass(),
                        e);
            else log.error("[{}] Error handling packet", connection.getProtocol(), e);
        }
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public Sender send(Object packet) {
        try {
            BridgeOptions options = connection.getProtocol().getOptions();
            byte[] packetBuf = options.serializeRaw(packet);
            if (packetBuf.length == 0)
                return new Sender(connection, ctx.newSucceededFuture(), false);

            buf.writeInt(packetBuf.length); // Packet Len
            buf.writeBytes(packetBuf);

            Sender sender = new Sender(connection, ctx.writeAndFlush(buf), true);
            buf = Unpooled.buffer();
            return sender;

        } catch (IllegalArgumentException e) {
            log.error("[{}] Tried to send an {}", connection.getProtocol(), e.getMessage());
            return new Sender(connection, ctx.newSucceededFuture(), false);
        } catch (Exception e) {
            log.error(
                    "[{}] Threw an error when trying to send: {}!",
                    connection.getProtocol(),
                    packet.getClass(),
                    e);
            return new Sender(connection, ctx.newSucceededFuture(), false);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String message = cause.getMessage();
        if (message == null) {
            ctx.close();
            connection.disconnect();
            return;
        }
        String[] parts = message.split(":");

        if (parts.length > 4 && parts[1].contains("proxium-proxy-cert-error")) {
            String[] fingerprints = parts[4].split("!=");

            log.warn(" Saved Fingerprint:     Server Fingerprint:");
            String images =
                    DrunkenBishop.inlineImages(
                            DrunkenBishop.parse(fingerprints[0]),
                            DrunkenBishop.parse(fingerprints[1]));
            for (String line : images.split("\n")) {
                log.warn(line);
            }

            log.error("Failed to connect to: {}:{}", parts[2], parts[3]);
            log.error(
                    "Server TLS fingerprint does not match saved fingerprint! This could be a MITM ATTACK!");
            log.error(" - https://en.wikipedia.org/wiki/Man-in-the-middle_attack");
            log.error(
                    "If you've reset your server configuration recently, you can probably ignore this and reset/remove the \"proxium.hosts\" file.");

            ctx.close();
            connection.disconnect();
        }

        if (cause instanceof SocketException) {
            ctx.close();
            connection.disconnect();
        }
    }
}
