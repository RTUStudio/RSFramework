package kr.rtustudio.framework.bukkit.api.core.scheduler;

import kr.rtustudio.framework.bukkit.api.RSPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Platform-specific (Spigot/Paper/Folia) scheduler abstraction interface. Supports sync/async,
 * delayed, and repeating task execution. On Folia, also supports {@link Location} and {@link
 * Entity} based region scheduling.
 *
 * <p>플랫폼별(Spigot/Paper/Folia) 스케줄러 추상화 인터페이스. 동기/비동기, 지연, 반복 실행을 지원하며, Folia에서는 Location/Entity 기반
 * 리전 스케줄링도 지원한다.
 */
public interface Scheduler {

    /**
     * Returns the scheduler owned by the specified plugin.
     *
     * <p>지정한 플러그인 소유의 스케줄러를 반환한다.
     *
     * @param plugin owning plugin
     * @return plugin scheduler
     */
    ScheduledTask plugin(RSPlugin plugin);

    /**
     * Executes synchronously on the main thread.
     *
     * <p>메인 스레드에서 동기 실행한다.
     *
     * @param consumer task to execute
     * @return scheduled task
     */
    <T> CompletableFuture<T> callSync(Callable<T> task);

    ScheduledTask sync(Consumer<ScheduledUnit> consumer);

    default ScheduledTask sync(Runnable runnable) {
        return sync(unit -> runnable.run());
    }

    ScheduledTask async(Consumer<ScheduledUnit> consumer);

    default ScheduledTask async(Runnable runnable) {
        return async(unit -> runnable.run());
    }

    default ScheduledTask delay(Consumer<ScheduledUnit> consumer, long delay) {
        return delay(consumer, delay, false);
    }

    default ScheduledTask delay(Runnable runnable, long delay) {
        return delay(runnable, delay, false);
    }

    ScheduledTask delay(Consumer<ScheduledUnit> consumer, long delay, boolean async);

    default ScheduledTask delay(Runnable runnable, long delay, boolean async) {
        return delay(unit -> runnable.run(), delay, async);
    }

    default ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long period) {
        return repeat(consumer, period, period, false);
    }

    default ScheduledTask repeat(Runnable runnable, long period) {
        return repeat(runnable, period, period, false);
    }

    default ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long period, boolean async) {
        return repeat(consumer, period, period, async);
    }

    default ScheduledTask repeat(Runnable runnable, long period, boolean async) {
        return repeat(runnable, period, period, async);
    }

    default ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long delay, long period) {
        return repeat(consumer, delay, period, false);
    }

    default ScheduledTask repeat(Runnable runnable, long delay, long period) {
        return repeat(runnable, delay, period, false);
    }

    ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long delay, long period, boolean async);

    default ScheduledTask repeat(Runnable runnable, long delay, long period, boolean async) {
        return repeat(unit -> runnable.run(), delay, period, async);
    }

    <T> CompletableFuture<T> callSync(Location location, Callable<T> task);

    ScheduledTask sync(Location location, Consumer<ScheduledUnit> consumer);

    default ScheduledTask sync(Location location, Runnable runnable) {
        return sync(location, unit -> runnable.run());
    }

    ScheduledTask delay(Location location, Consumer<ScheduledUnit> consumer, long delay);

    default ScheduledTask delay(Location location, Runnable runnable, long delay) {
        return delay(location, unit -> runnable.run(), delay);
    }

    default ScheduledTask repeat(Location location, Consumer<ScheduledUnit> consumer, long period) {
        return repeat(location, consumer, period, period);
    }

    default ScheduledTask repeat(Location location, Runnable runnable, long period) {
        return repeat(location, runnable, period, period);
    }

    ScheduledTask repeat(
            Location location, Consumer<ScheduledUnit> consumer, long delay, long period);

    default ScheduledTask repeat(Location location, Runnable runnable, long delay, long period) {
        return repeat(location, unit -> runnable.run(), delay, period);
    }

    <T> CompletableFuture<T> callSync(Entity entity, Callable<T> task);

    ScheduledTask sync(Entity entity, Consumer<ScheduledUnit> consumer);

    default ScheduledTask sync(Entity entity, Runnable runnable) {
        return sync(entity, unit -> runnable.run());
    }

    ScheduledTask delay(Entity entity, Consumer<ScheduledUnit> consumer, long delay);

    default ScheduledTask delay(Entity entity, Runnable runnable, long delay) {
        return delay(entity, unit -> runnable.run(), delay);
    }

    default ScheduledTask repeat(Entity entity, Consumer<ScheduledUnit> consumer, long period) {
        return repeat(entity, consumer, period, period);
    }

    default ScheduledTask repeat(Entity entity, Runnable runnable, long period) {
        return repeat(entity, runnable, period, period);
    }

    ScheduledTask repeat(Entity entity, Consumer<ScheduledUnit> consumer, long delay, long period);

    default ScheduledTask repeat(Entity entity, Runnable runnable, long delay, long period) {
        return repeat(entity, unit -> runnable.run(), delay, period);
    }
}
