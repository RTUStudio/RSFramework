package kr.rtustudio.bridge.redis;

import kr.rtustudio.bridge.Bridge;

import java.util.function.Supplier;

/**
 * Redis-based bridge interface.
 *
 * <pre>{@code
 * Redis bridge = registry.get(Redis.class);
 * bridge.publish("rsf:shop", new BuyPacket(...));
 * }</pre>
 */
public interface Redis extends Bridge {

    /**
     * Executes the given action within a distributed Redis lock.
     *
     * @param key the lock key
     * @param action the action to execute and return a value
     * @param <T> the return type
     * @return the result of the action, or null if the lock could not be acquired
     */
    <T> T withLock(String key, Supplier<T> action);

    /**
     * Executes the given action within a distributed Redis lock with specific timings.
     *
     * @param key the lock key
     * @param waitTime time to wait for lock acquisition
     * @param leaseTime time to hold the lock
     * @param timeUnit the time unit for the timings
     * @param action the action to execute and return a value
     * @param <T> the return type
     * @return the result of the action, or null if the lock could not be acquired
     */
    <T> T withLock(
            String key,
            long waitTime,
            long leaseTime,
            java.util.concurrent.TimeUnit timeUnit,
            Supplier<T> action);

    /**
     * Executes the given action within a distributed Redis lock.
     *
     * @param key the lock key
     * @param action the action to execute
     * @return true if the lock was acquired and the action executed, false otherwise
     */
    boolean withLock(String key, Runnable action);

    /**
     * Executes the given action within a distributed Redis lock with specific timings.
     *
     * @param key the lock key
     * @param waitTime time to wait for lock acquisition
     * @param leaseTime time to hold the lock
     * @param timeUnit the time unit for the timings
     * @param action the action to execute
     * @return true if the lock was acquired and the action executed, false otherwise
     */
    boolean withLock(
            String key,
            long waitTime,
            long leaseTime,
            java.util.concurrent.TimeUnit timeUnit,
            Runnable action);

    /**
     * Attempts to execute the given action within a distributed Redis lock only once without
     * waiting.
     *
     * @param key the lock key
     * @param action the action to execute
     * @return true if the lock was acquired and the action executed, false otherwise
     */
    boolean tryLockOnce(String key, Runnable action);

    /**
     * Attempts to execute the given action within a distributed Redis lock only once without
     * waiting, with specific lease time.
     *
     * @param key the lock key
     * @param leaseTime time to hold the lock
     * @param timeUnit the time unit for the timings
     * @param action the action to execute
     * @return true if the lock was acquired and the action executed, false otherwise
     */
    boolean tryLockOnce(
            String key, long leaseTime, java.util.concurrent.TimeUnit timeUnit, Runnable action);
}
