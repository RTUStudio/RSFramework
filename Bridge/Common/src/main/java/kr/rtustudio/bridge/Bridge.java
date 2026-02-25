package kr.rtustudio.bridge;

import java.util.function.Consumer;

public interface Bridge {

    void register(BridgeChannel channel, Class<?>... types);

    void subscribe(BridgeChannel channel, Consumer<Object> handler);

    void publish(BridgeChannel channel, Object message);

    void unsubscribe(BridgeChannel channel);

    boolean isLoaded();

    void close();
}
