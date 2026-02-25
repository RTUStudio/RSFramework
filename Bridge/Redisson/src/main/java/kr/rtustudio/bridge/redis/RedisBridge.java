package kr.rtustudio.bridge.redis;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.redis.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j(topic = "RSF/Bridge/Redis")
public class RedisBridge implements Redis {

    private final RedisConfig config;
    private final BridgeOptions options;
    private final Object2BooleanOpenHashMap<BridgeChannel> registeredChannels =
            new Object2BooleanOpenHashMap<>();
    private Redisson redisson;
    private boolean loaded = false;

    public RedisBridge(RedisConfig config, ClassLoader classLoader) {
        this(config, BridgeOptions.defaults(classLoader));
    }

    public RedisBridge(RedisConfig config, BridgeOptions options) {
        this.config = config;
        this.options = options;

        if (!config.isEnabled()) return;

        try {
            this.redisson = new Redisson(config);
            this.loaded = true;
        } catch (Exception e) {
            log.error("Failed to initialize Redis connection", e);
            this.loaded = false;
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    private synchronized Redisson getRedisson() {
        if (loaded) return redisson;
        throw new IllegalStateException("Redis bridge is not loaded");
    }

    @Override
    public void register(BridgeChannel channel, Class<?>... types) {
        options.register(channel, types);
        registeredChannels.put(channel, true);
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        if (!registeredChannels.containsKey(channel)) {
            log.warn(
                    "No types registered for channel: {}. Call register() before subscribe().",
                    channel);
            return;
        }
        getRedisson()
                .subscribe(
                        channel.toString(),
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
    public void publish(BridgeChannel channel, Object message) {
        if (!registeredChannels.containsKey(channel)) {
            log.warn(
                    "No types registered for channel: {}. Call register() before publish().",
                    channel);
            return;
        }
        getRedisson().publish(channel.toString(), options.encode(channel, message));
    }

    @Override
    public void unsubscribe(BridgeChannel channel) {
        if (redisson != null) {
            redisson.unsubscribe(channel.toString());
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

    @Override
    public <T> T withLock(String key, Supplier<T> action) {
        return withLock(
                key,
                config.getLockWaitTime(),
                config.getLockLeaseTime(),
                TimeUnit.MILLISECONDS,
                action);
    }

    @Override
    public <T> T withLock(
            String key, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> action) {
        org.redisson.api.RLock lock = getRedisson().getLock("rsf:lock:" + key);
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                try {
                    return action.get();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                log.warn("Failed to acquire lock for key: {}", key);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock interrupted for key: {}", key);
            return null;
        }
    }

    @Override
    public boolean withLock(String key, Runnable action) {
        return withLock(
                key,
                config.getLockWaitTime(),
                config.getLockLeaseTime(),
                TimeUnit.MILLISECONDS,
                action);
    }

    @Override
    public boolean withLock(
            String key, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable action) {
        org.redisson.api.RLock lock = getRedisson().getLock("rsf:lock:" + key);
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                try {
                    action.run();
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                log.warn("Failed to acquire lock for key: {}", key);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock interrupted for key: {}", key);
            return false;
        }
    }

    @Override
    public boolean tryLockOnce(String key, Runnable action) {
        return tryLockOnce(key, config.getLockLeaseTime(), TimeUnit.MILLISECONDS, action);
    }

    @Override
    public boolean tryLockOnce(String key, long leaseTime, TimeUnit timeUnit, Runnable action) {
        org.redisson.api.RLock lock = getRedisson().getLock("rsf:lock:" + key);
        try {
            if (lock.tryLock(0, leaseTime, timeUnit)) {
                try {
                    action.run();
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
