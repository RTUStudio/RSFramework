package kr.rtustudio.bridge.redis;

import kr.rtustudio.bridge.Bridge;

import java.util.function.Supplier;

public interface Redis extends Bridge {

    <T> T withLock(String key, Supplier<T> action);

    <T> T withLock(
            String key,
            long waitTime,
            long leaseTime,
            java.util.concurrent.TimeUnit timeUnit,
            Supplier<T> action);

    boolean withLock(String key, Runnable action);

    boolean withLock(
            String key,
            long waitTime,
            long leaseTime,
            java.util.concurrent.TimeUnit timeUnit,
            Runnable action);

    boolean tryLockOnce(String key, Runnable action);

    boolean tryLockOnce(
            String key, long leaseTime, java.util.concurrent.TimeUnit timeUnit, Runnable action);
}
