package kr.rtustudio.bridge.redis;

import kr.rtustudio.bridge.Bridge;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Redis-based bridge interface providing pub/sub communication and distributed locking.
 *
 * <p>Redis 기반 브릿지 인터페이스. Pub/Sub 통신 및 분산 락을 제공한다.
 */
public interface Redis extends Bridge {

    <T> T withLock(@NotNull String key, @NotNull Supplier<T> action);

    <T> T withLock(
            @NotNull String key,
            long waitTime,
            long leaseTime,
            @NotNull TimeUnit timeUnit,
            @NotNull Supplier<T> action);

    boolean withLock(@NotNull String key, @NotNull Runnable action);

    boolean withLock(
            @NotNull String key,
            long waitTime,
            long leaseTime,
            @NotNull TimeUnit timeUnit,
            @NotNull Runnable action);

    boolean tryLockOnce(@NotNull String key, @NotNull Runnable action);

    boolean tryLockOnce(
            @NotNull String key,
            long leaseTime,
            @NotNull TimeUnit timeUnit,
            @NotNull Runnable action);
}
