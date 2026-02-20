package kr.rtustudio.broker;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for {@link Broker} instances, keyed by interface type.
 *
 * <pre>{@code
 * registry.register(RedisBroker.class, new RedisBrokerImpl(config));
 * RedisBroker broker = registry.get(RedisBroker.class);
 * }</pre>
 */
public class BrokerRegistry {

    private final Map<Class<? extends Broker>, Broker> brokers = new HashMap<>();

    public <T extends Broker> void register(@NotNull Class<T> type, @NotNull T broker) {
        brokers.put(type, broker);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Broker> T get(@NotNull Class<T> type) {
        return (T) brokers.get(type);
    }

    public boolean has(@NotNull Class<? extends Broker> type) {
        return brokers.containsKey(type);
    }

    public void closeAll() {
        brokers.values().forEach(Broker::close);
        brokers.clear();
    }
}
