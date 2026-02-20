package kr.rtustudio.broker.protoweaver.api.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A wrapper that allows for closing the connection after the previously sent packets is done
 * sending.
 */
@RequiredArgsConstructor
public class Sender {

    public static Sender NULL = new Sender(null, null, false);

    private final ProtoConnection connection;
    private final ChannelFuture future;
    @Getter private final boolean success;

    /** Closes the connection after the previously sent packets is done sending. */
    public void disconnect() {
        if (future != null)
            future.addListener((ChannelFutureListener) channelFuture -> connection.disconnect());
    }
}
