package kr.rtuserver.framework.bukkit.api.core.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

/**
 * Fluent, chainable scheduler pipeline for tasks.
 * Naming aligned with Paper's Task semantics.
 */
public interface ScheduledTask {

    ScheduledTask sync(Consumer<ScheduledUnit> task);

    ScheduledTask sync(Runnable task);

    ScheduledTask async(Consumer<ScheduledUnit> task);

    ScheduledTask async(Runnable task);

    ScheduledTask delay(Consumer<ScheduledUnit> task, long delay);

    ScheduledTask delay(Runnable task, long delay);

    ScheduledTask delay(Consumer<ScheduledUnit> task, long delay, boolean async);

    ScheduledTask delay(Runnable task, long delay, boolean async);

    ScheduledTask repeat(Consumer<ScheduledUnit> task, long delay, long period);

    ScheduledTask repeat(Runnable task, long delay, long period);

    ScheduledTask repeat(Consumer<ScheduledUnit> task, long delay, long period, boolean async);

    ScheduledTask repeat(Runnable task, long delay, long period, boolean async);

    // Per-step contextual variants (sync-only for context-bound execution)
    ScheduledTask sync(Location location, Consumer<ScheduledUnit> task);

    ScheduledTask sync(Location location, Runnable task);

    ScheduledTask sync(Entity entity, Consumer<ScheduledUnit> task);

    ScheduledTask sync(Entity entity, Runnable task);

    ScheduledTask delay(Location location, Consumer<ScheduledUnit> task, long delay);

    ScheduledTask delay(Location location, Runnable task, long delay);

    ScheduledTask delay(Entity entity, Consumer<ScheduledUnit> task, long delay);

    ScheduledTask delay(Entity entity, Runnable task, long delay);

    ScheduledTask repeat(Location location, Consumer<ScheduledUnit> task, long delay, long period);

    ScheduledTask repeat(Location location, Runnable task, long delay, long period);

    ScheduledTask repeat(Entity entity, Consumer<ScheduledUnit> task, long delay, long period);

    ScheduledTask repeat(Entity entity, Runnable task, long delay, long period);

    void cancel();

}
