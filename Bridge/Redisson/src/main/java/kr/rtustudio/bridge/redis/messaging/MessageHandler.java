package kr.rtustudio.bridge.redis.messaging;

@FunctionalInterface
public interface MessageHandler<T> {

    void onMessage(String channel, T message);
}
