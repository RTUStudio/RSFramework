package kr.rtustudio.bridge.redis.messaging;

/**
 * Functional interface for handling messages received from a Redis channel.
 *
 * @param <T> the message type
 */
@FunctionalInterface
public interface MessageHandler<T> {

    void onMessage(String channel, T message);
}
