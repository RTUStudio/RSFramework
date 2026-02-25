package kr.rtustudio.bridge.proxium.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.protocol.handler.InternalConnectionHandler;
import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j(topic = "Proxium")
public class ConnectionDeterminer extends ByteToMessageDecoder {

    private final boolean tlsEnabled;

    public ConnectionDeterminer() {
        this.tlsEnabled = false;
    }

    public ConnectionDeterminer(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public static void registerToPipeline(Channel channel) {
        channel.pipeline().addFirst("connectionDeterminer", new ConnectionDeterminer());
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
                if (handler.getKey().equals("connectionDeterminer")) continue;
                pipeline.remove(handler.getValue());
            }
        }

        if (SSLContext.getContext() != null && enableTLS(buf)) {
            pipeline.addLast("tls", SSLContext.getContext().newHandler(ctx.alloc()));
            pipeline.addLast("tlsConnectionDeterminer", new ConnectionDeterminer(true));
            pipeline.remove(this);
            return;
        }

        if (isProxium(magic1, magic2)) {
            if (SSLContext.getContext() != null && !tlsEnabled) {
                ctx.close();
                return;
            }

            new Connection(InternalConnectionHandler.getProtocol(), Side.SERVER, ctx.channel());
            buf.readerIndex(2);
            pipeline.remove(this);
            return;
        }

        ctx.close();
    }

    private boolean isMinecraft(int magic1, int magic2) {
        return magic1 > 0 && magic2 == 0;
    }

    private boolean enableTLS(ByteBuf buf) {
        if (tlsEnabled) return false;
        return SslHandler.isEncrypted(buf);
    }

    private boolean isProxium(int magic1, int magic2) {
        return magic1 == 0 && magic2 == ProxiumConstants.PROXIUM_MAGIC_BYTE;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Proxy rejected TLS certificate. Closing connection");
        log.error("TLS certificate error", cause);
        ctx.close();
    }
}
