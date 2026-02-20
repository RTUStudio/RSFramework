package kr.rtustudio.broker.redis;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kr.rtustudio.broker.BrokerOptions;
import kr.rtustudio.broker.redis.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class Redis implements kr.rtustudio.broker.Redis {

    private final Redisson redisson;
    private final BrokerOptions options;
    private final Object2BooleanOpenHashMap<String> registeredChannels =
            new Object2BooleanOpenHashMap<>();

    public Redis(RedisConfig config, ClassLoader classLoader) {
        this(config, BrokerOptions.defaults(classLoader));
    }

    public Redis(RedisConfig config, BrokerOptions options) {
        this.redisson = new Redisson(config);
        this.options = options;
    }

    @Override
    public void register(String channel, Class<?>... types) {
        for (Class<?> type : types) options.register(type);
        registeredChannels.put(channel, true);
    }

    @Override
    public void subscribe(String channel, Consumer<Object> handler) {
        if (!registeredChannels.containsKey(channel)) {
            log.warn(
                    "No types registered for channel: {}. Call register() before subscribe().",
                    channel);
            return;
        }
        redisson.subscribe(
                channel,
                byte[].class,
                (ch, frame) -> {
                    try {
                        handler.accept(options.decode(frame));
                    } catch (Exception e) {
                        log.error("Failed to decode message on channel: {}", ch, e);
                    }
                });
    }

    @Override
    public void publish(String channel, Object message) {
        if (!registeredChannels.containsKey(channel)) {
            log.warn(
                    "No types registered for channel: {}. Call register() before publish().",
                    channel);
            return;
        }
        redisson.publish(channel, options.encode(channel, message));
    }

    @Override
    public void unsubscribe(String channel) {
        redisson.unsubscribe(channel);
        registeredChannels.removeBoolean(channel);
    }

    @Override
    public void close() {
        registeredChannels.clear();
        redisson.shutdown();
    }
}
