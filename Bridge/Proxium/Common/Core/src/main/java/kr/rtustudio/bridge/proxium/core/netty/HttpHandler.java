package kr.rtustudio.bridge.proxium.core.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                FullHttpResponse response =
                        new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1,
                                HttpResponseStatus.CONTINUE,
                                Unpooled.EMPTY_BUFFER);
                ctx.write(response);
            }
        }

        if (msg instanceof HttpContent) {

            if (msg instanceof LastHttpContent lastHttpContent) {
                if (!writeResponse(lastHttpContent, ctx)) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                            .addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response =
                new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        currentObj.decoderResult().isSuccess()
                                ? HttpResponseStatus.OK
                                : HttpResponseStatus.BAD_REQUEST,
                        Unpooled.copiedBuffer("", CharsetUtil.UTF_8));

        if (keepAlive) {
            response.headers()
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);
        return keepAlive;
    }
}
