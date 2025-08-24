package kr.rtuserver.framework.bukkit.api.core.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public interface BukkitScheduler {

    // Global context
    ScheduledTask sync(Plugin plugin, Consumer<ScheduledUnit> consumer);

    default ScheduledTask sync(Plugin plugin, Runnable runnable) {
        return sync(plugin, unit -> runnable.run());
    }

    ScheduledTask async(Plugin plugin, Consumer<ScheduledUnit> consumer);

    default ScheduledTask async(Plugin plugin, Runnable runnable) {
        return async(plugin, unit -> runnable.run());
    }

    ScheduledTask delay(Plugin plugin, Consumer<ScheduledUnit> consumer, long delay, boolean async);

    default ScheduledTask delay(Plugin plugin, Runnable runnable, long delay, boolean async) {
        return delay(plugin, unit -> runnable.run(), delay, async);
    }

    default ScheduledTask delay(Plugin plugin, Consumer<ScheduledUnit> consumer, long delay) {
        return delay(plugin, consumer, delay, false);
    }

    default ScheduledTask delay(Plugin plugin, Runnable runnable, long delay) {
        return delay(plugin, runnable, delay, false);
    }

    ScheduledTask repeat(Plugin plugin, Consumer<ScheduledUnit> consumer, long delay, long period, boolean async);

    default ScheduledTask repeat(Plugin plugin, Runnable runnable, long delay, long period, boolean async) {
        return repeat(plugin, unit -> runnable.run(), delay, period, async);
    }

    default ScheduledTask repeat(Plugin plugin, Consumer<ScheduledUnit> consumer, long period) {
        return repeat(plugin, consumer, period, period, false);
    }

    default ScheduledTask repeat(Plugin plugin, Runnable runnable, long period) {
        return repeat(plugin, runnable, period, period, false);
    }

    default ScheduledTask repeat(Plugin plugin, Consumer<ScheduledUnit> consumer, long period, boolean async) {
        return repeat(plugin, consumer, period, period, async);
    }

    default ScheduledTask repeat(Plugin plugin, Runnable runnable, long period, boolean async) {
        return repeat(plugin, runnable, period, period, async);
    }

    // Location context
    ScheduledTask sync(Plugin plugin, Location location, Consumer<ScheduledUnit> consumer);

    default ScheduledTask sync(Plugin plugin, Location location, Runnable runnable) {
        return sync(plugin, location, unit -> runnable.run());
    }

    ScheduledTask delay(Plugin plugin, Location location, Consumer<ScheduledUnit> consumer, long delay);

    default ScheduledTask delay(Plugin plugin, Location location, Runnable runnable, long delay) {
        return delay(plugin, location, unit -> runnable.run(), delay);
    }

    ScheduledTask repeat(Plugin plugin, Location location, Consumer<ScheduledUnit> consumer, long delay, long period);

    default ScheduledTask repeat(Plugin plugin, Location location, Runnable runnable, long delay, long period) {
        return repeat(plugin, location, unit -> runnable.run(), delay, period);
    }

    default ScheduledTask repeat(Plugin plugin, Location location, Consumer<ScheduledUnit> consumer, long period) {
        return repeat(plugin, location, consumer, period, period);
    }

    default ScheduledTask repeat(Plugin plugin, Location location, Runnable runnable, long period) {
        return repeat(plugin, location, runnable, period, period);
    }

    // Entity context
    ScheduledTask sync(Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer);

    default ScheduledTask sync(Plugin plugin, Entity entity, Runnable runnable) {
        return sync(plugin, entity, unit -> runnable.run());
    }

    ScheduledTask delay(Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long delay);

    default ScheduledTask delay(Plugin plugin, Entity entity, Runnable runnable, long delay) {
        return delay(plugin, entity, unit -> runnable.run(), delay);
    }

    ScheduledTask repeat(Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long delay, long period);

    default ScheduledTask repeat(Plugin plugin, Entity entity, Runnable runnable, long delay, long period) {
        return repeat(plugin, entity, unit -> runnable.run(), delay, period);
    }

    default ScheduledTask repeat(Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long period) {
        return repeat(plugin, entity, consumer, period, period);
    }

    default ScheduledTask repeat(Plugin plugin, Entity entity, Runnable runnable, long period) {
        return repeat(plugin, entity, runnable, period, period);
    }

}
