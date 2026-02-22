package kr.rtustudio.bridge.protoweaver.core.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.protocol.Side;
import kr.rtustudio.bridge.protoweaver.api.protocol.handler.InternalConnectionHandler;
import kr.rtustudio.bridge.protoweaver.api.util.ProtoConstants;
import kr.rtustudio.bridge.protoweaver.api.util.ProtoLogger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class ProtoDeterminer extends ByteToMessageDecoder {

    private final boolean tlsEnabled;

    public ProtoDeterminer() {
        this.tlsEnabled = false;
    }

    public ProtoDeterminer(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public static void registerToPipeline(Channel channel) {
        channel.pipeline().addFirst("protoDeterminer", new ProtoDeterminer());
    }

    @Override
    @SneakyThrows
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
        if (buf.readableBytes() < 5) {
            return;
        }

        ChannelPipeline pipeline = ctx.pipeline();
        int magic1 = buf.getUnsignedByte(buf.readerIndex());
        int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);

        if (isMinecraft(magic1, magic2)) {
            pipeline.remove(this);
            return;
        }

        if (!tlsEnabled) {
            for (Map.Entry<String, ChannelHandler> handler : pipeline.toMap().entrySet()) {
                if (handler.getKey().equals("protoDeterminer")) continue;
                pipeline.remove(handler.getValue());
            }
        }

        // Upstream protocol
        if (SSLContext.getContext() != null && enableTLS(buf)) {
            pipeline.addLast("tls", SSLContext.getContext().newHandler(ctx.alloc()));
            pipeline.addLast("tlsProtoDeterminer", new ProtoDeterminer(true));
            pipeline.remove(this);
            return;
        }

        // Downstream protocol
        if (isProtoWeaver(magic1, magic2)) {
            if (SSLContext.getContext() != null && !tlsEnabled) {
                ctx.close();
                return;
            }

            new ProtoConnection(
                    InternalConnectionHandler.getProtocol(), Side.SERVER, ctx.channel());
            buf.readerIndex(2);
            pipeline.remove(this);
            return;
        }

        ctx.close();
    }

    // Check if packets is minecraft handshake -
    // https://wiki.vg/Protocol#Handshaking
    private boolean isMinecraft(int magic1, int magic2) {
        return magic1 > 0 && magic2 == 0;
    }

    private boolean enableTLS(ByteBuf buf) {
        if (tlsEnabled) return false;
        return SslHandler.isEncrypted(buf);
    }

    private boolean isProtoWeaver(int magic1, int magic2) {
        return magic1 == 0 && magic2 == ProtoConstants.PROTOWEAVER_MAGIC_BYTE;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ProtoLogger.warn("Client rejected TLS certificate. Closing connection");
        log.error("TLS certificate error", cause);
        ctx.close();
    }
}
