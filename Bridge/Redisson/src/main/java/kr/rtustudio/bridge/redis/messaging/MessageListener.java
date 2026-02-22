package kr.rtustudio.bridge.redis.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal adapter that bridges Redisson's listener API to {@link MessageHandler}.
 *
 * @param <T> the message type
 */
@Slf4j
@RequiredArgsConstructor
public class MessageListener<T> implements org.redisson.api.listener.MessageListener<T> {

    private final MessageHandler<T> handler;

    @Override
    public void onMessage(CharSequence channel, T message) {
        try {
            handler.onMessage(channel.toString(), message);
        } catch (Exception e) {
            log.error("Error handling message on channel: {}", channel, e);
        }
    }
}
