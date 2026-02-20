package kr.rtustudio.broker;

import java.util.function.Consumer;

public interface Broker {

    void register(String channel, Class<?>... types);

    void subscribe(String channel, Consumer<Object> handler);

    void publish(String channel, Object message);

    void unsubscribe(String channel);

    void close();
}
