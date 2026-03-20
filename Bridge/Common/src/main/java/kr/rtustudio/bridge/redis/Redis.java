package kr.rtustudio.bridge.redis;

import kr.rtustudio.bridge.Bridge;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

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
