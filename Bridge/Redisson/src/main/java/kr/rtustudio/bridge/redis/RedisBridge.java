package kr.rtustudio.bridge.redis;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.redis.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j(topic = "RSF/Bridge/Redis")
public class RedisBridge implements Redis {

    private final RedisConfig config;
    private final BridgeOptions options;
    private final Object2BooleanOpenHashMap<BridgeChannel> registeredChannels =
            new Object2BooleanOpenHashMap<>();
    private final byte[] serverIdBytes;

    private Redisson redisson;
    private boolean loaded = false;

    public RedisBridge(ClassLoader classLoader, RedisConfig config) {
        this(config, new BridgeOptions(classLoader));
    }

    public RedisBridge(RedisConfig config, BridgeOptions options) {
        this.config = config;
        this.options = options;

        UUID uuid = UUID.randomUUID();
        this.serverIdBytes = new byte[16];
        ByteBuffer.wrap(serverIdBytes)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());

        if (!config.isEnabled()) {
            this.loaded = false;
            return;
        }

        try {
            this.redisson = new Redisson(config);
            this.loaded = true;
        } catch (Exception e) {
            log.error("Failed to initialize Redis connection", e);
            this.loaded = false;
        }
    }

    private synchronized Redisson getRedisson() {
        if (loaded) return redisson;
        throw new IllegalStateException("Redis bridge is not loaded");
    }

    @Override
    public boolean isConnected() {
        return loaded && redisson != null;
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
                                if (isSelf(frame)) return;
                                byte[] payload = new byte[frame.length - 16];
                                System.arraycopy(frame, 16, payload, 0, payload.length);
                                handler.accept(options.decode(payload));
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
        byte[] encoded = options.encode(channel, message);
        byte[] wrapped = new byte[16 + encoded.length];
        System.arraycopy(serverIdBytes, 0, wrapped, 0, 16);
        System.arraycopy(encoded, 0, wrapped, 16, encoded.length);
        getRedisson().publish(channel.toString(), wrapped);
    }

    private boolean isSelf(byte[] frame) {
        if (frame.length <= 16) return false;
        for (int i = 0; i < 16; i++) {
            if (frame[i] != serverIdBytes[i]) return false;
        }
        return true;
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
        if (redisson != null) {
            redisson.shutdown();
        }
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
