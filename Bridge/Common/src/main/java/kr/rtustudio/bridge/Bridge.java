package kr.rtustudio.bridge;

import java.util.function.Consumer;

public interface Bridge {

    void register(String channel, Class<?>... types);

    void subscribe(String channel, Consumer<Object> handler);

    void publish(String channel, Object message);

    void unsubscribe(String channel);

    void close();
}
