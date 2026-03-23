package kr.rtustudio.bridge.proxium.api.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A wrapper that allows for closing the connection after the previously sent packets finish.
 *
 * <p>이전에 전송된 패킷 전송 완료 후 연결을 닫을 수 있는 래퍼.
 */
@RequiredArgsConstructor
public class Sender {

    private final Connection connection;
    private final ChannelFuture future;
    @Getter private final boolean success;

    /**
     * Closes the connection after the previously sent packets finish sending.
     *
     * <p>이전에 전송된 패킷 전송 완료 후 연결을 닫는다.
     */
    public void disconnect() {
        if (future != null)
            future.addListener((ChannelFutureListener) channelFuture -> connection.disconnect());
    }
}
