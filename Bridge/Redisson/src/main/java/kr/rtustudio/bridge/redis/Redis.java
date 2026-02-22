package kr.rtustudio.bridge.redis;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.redis.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class Redis implements kr.rtustudio.bridge.Redis {

    private Redisson redisson;
    private final RedisConfig config;
    private final BridgeOptions options;
    private final Object2BooleanOpenHashMap<String> registeredChannels =
            new Object2BooleanOpenHashMap<>();

    public Redis(RedisConfig config, ClassLoader classLoader) {
        this(config, BridgeOptions.defaults(classLoader));
    }

    public Redis(RedisConfig config, BridgeOptions options) {
        this.config = config;
        this.options = options;
    }

    private synchronized Redisson getRedisson() {
        if (redisson == null) {
            redisson = new Redisson(config);
        }
        return redisson;
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
        getRedisson()
                .subscribe(
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
        getRedisson().publish(channel, options.encode(channel, message));
    }

    @Override
    public void unsubscribe(String channel) {
        if (redisson != null) {
            redisson.unsubscribe(channel);
        }
        registeredChannels.removeBoolean(channel);
    }

    @Override
    public void close() {
        registeredChannels.clear();
        if (redisson != null) {
            redisson.shutdown();
            redisson = null;
        }
    }
}
