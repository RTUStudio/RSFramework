package kr.rtuserver.framework.bukkit.api.scheduler;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class CraftScheduler {

    private static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static ScheduledTask sync(RSPlugin plugin, Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().plugin(plugin).sync(consumer);
    }

    public static ScheduledTask sync(RSPlugin plugin, Runnable runnable) {
        return framework().getScheduler().plugin(plugin).sync(runnable);
    }

    public static ScheduledTask sync(Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().sync(consumer);
    }

    public static ScheduledTask sync(Runnable runnable) {
        return framework().getScheduler().sync(runnable);
    }

    public static ScheduledTask async(RSPlugin plugin, Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().plugin(plugin).async(consumer);
    }

    public static ScheduledTask async(RSPlugin plugin, Runnable runnable) {
        return framework().getScheduler().plugin(plugin).async(runnable);
    }

    public static ScheduledTask async(Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().async(consumer);
    }

    public static ScheduledTask async(Runnable runnable) {
        return framework().getScheduler().async(runnable);
    }

    public static ScheduledTask delay(RSPlugin plugin, Consumer<ScheduledUnit> consumer, long delay) {
        return framework().getScheduler().plugin(plugin).delay(consumer, delay);
    }

    public static ScheduledTask delay(RSPlugin plugin, Runnable runnable, long delay) {
        return framework().getScheduler().plugin(plugin).delay(runnable, delay);
    }

    public static ScheduledTask delay(Consumer<ScheduledUnit> consumer, long delay) {
        return framework().getScheduler().delay(consumer, delay);
    }

    public static ScheduledTask delay(Runnable runnable, long delay) {
        return framework().getScheduler().delay(runnable, delay);
    }

    public static ScheduledTask delay(RSPlugin plugin, Consumer<ScheduledUnit> consumer, long delay, boolean async) {
        return framework().getScheduler().plugin(plugin).delay(consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().plugin(plugin).delay(runnable, delay, async);
    }

    public static ScheduledTask delay(Consumer<ScheduledUnit> consumer, long delay, boolean async) {
        return framework().getScheduler().delay(consumer, delay, async);
    }

    public static ScheduledTask delay(Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(runnable, delay, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Consumer<ScheduledUnit> consumer, long period) {
        return framework().getScheduler().plugin(plugin).repeat(consumer, period, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long period) {
        return framework().getScheduler().plugin(plugin).repeat(runnable, period, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Consumer<ScheduledUnit> consumer, long period, boolean async) {
        return framework().getScheduler().plugin(plugin).repeat(consumer, period, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long period, boolean async) {
        return framework().getScheduler().plugin(plugin).repeat(runnable, period, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return framework().getScheduler().plugin(plugin).repeat(consumer, delay, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return framework().getScheduler().plugin(plugin).repeat(runnable, delay, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Consumer<ScheduledUnit> consumer, long delay, long period, boolean async) {
        return framework().getScheduler().plugin(plugin).repeat(consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().plugin(plugin).repeat(runnable, delay, period, async);
    }

    public static ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long period) {
        return framework().getScheduler().repeat(consumer, period, period);
    }

    public static ScheduledTask repeat(Runnable runnable, long period) {
        return framework().getScheduler().repeat(runnable, period, period);
    }

    public static ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long period, boolean async) {
        return framework().getScheduler().repeat(consumer, period, period, async);
    }

    public static ScheduledTask repeat(Runnable runnable, long period, boolean async) {
        return framework().getScheduler().repeat(runnable, period, period, async);
    }

    public static ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long delay, long period) {
        return framework().getScheduler().repeat(consumer, delay, period);
    }

    public static ScheduledTask repeat(Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(runnable, delay, period);
    }

    public static ScheduledTask repeat(Consumer<ScheduledUnit> consumer, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(consumer, delay, period, async);
    }

    public static ScheduledTask repeat(Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(runnable, delay, period, async);
    }

    public static ScheduledTask sync(RSPlugin plugin, Location location, Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().plugin(plugin).sync(location, consumer);
    }

    public static ScheduledTask sync(RSPlugin plugin, Location location, Runnable runnable) {
        return framework().getScheduler().plugin(plugin).sync(location, runnable);
    }

    public static ScheduledTask sync(Location location, Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().sync(location, consumer);
    }

    public static ScheduledTask sync(Location location, Runnable runnable) {
        return framework().getScheduler().sync(location, runnable);
    }

    public static ScheduledTask delay(RSPlugin plugin, Location location, Consumer<ScheduledUnit> consumer, long delay) {
        return framework().getScheduler().plugin(plugin).delay(location, consumer, delay);
    }

    public static ScheduledTask delay(RSPlugin plugin, Location location, Runnable runnable, long delay) {
        return framework().getScheduler().plugin(plugin).delay(location, runnable, delay);
    }

    public static ScheduledTask delay(Location location, Consumer<ScheduledUnit> consumer, long delay) {
        return framework().getScheduler().delay(location, consumer, delay);
    }

    public static ScheduledTask delay(Location location, Runnable runnable, long delay) {
        return framework().getScheduler().delay(location, runnable, delay);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Consumer<ScheduledUnit> consumer, long period) {
        return framework().getScheduler().plugin(plugin).repeat(location, consumer, period, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Runnable runnable, long period) {
        return framework().getScheduler().plugin(plugin).repeat(location, runnable, period, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return framework().getScheduler().plugin(plugin).repeat(location, consumer, delay, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Runnable runnable, long delay, long period) {
        return framework().getScheduler().plugin(plugin).repeat(location, runnable, delay, period);
    }

    public static ScheduledTask repeat(Location location, Consumer<ScheduledUnit> consumer, long period) {
        return framework().getScheduler().repeat(location, consumer, period, period);
    }

    public static ScheduledTask repeat(Location location, Runnable runnable, long period) {
        return framework().getScheduler().repeat(location, runnable, period, period);
    }

    public static ScheduledTask repeat(Location location, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return framework().getScheduler().repeat(location, consumer, delay, period);
    }

    public static ScheduledTask repeat(Location location, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(location, runnable, delay, period);
    }

    public static ScheduledTask sync(RSPlugin plugin, Entity entity, Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().plugin(plugin).sync(entity, consumer);
    }

    public static ScheduledTask sync(RSPlugin plugin, Entity entity, Runnable runnable) {
        return framework().getScheduler().plugin(plugin).sync(entity, runnable);
    }

    public static ScheduledTask sync(Entity entity, Consumer<ScheduledUnit> consumer) {
        return framework().getScheduler().sync(entity, consumer);
    }

    public static ScheduledTask sync(Entity entity, Runnable runnable) {
        return framework().getScheduler().sync(entity, runnable);
    }

    public static ScheduledTask delay(RSPlugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long delay) {
        return framework().getScheduler().plugin(plugin).delay(entity, consumer, delay);
    }

    public static ScheduledTask delay(RSPlugin plugin, Entity entity, Runnable runnable, long delay) {
        return framework().getScheduler().plugin(plugin).delay(entity, runnable, delay);
    }

    public static ScheduledTask delay(Entity entity, Consumer<ScheduledUnit> consumer, long delay) {
        return framework().getScheduler().delay(entity, consumer, delay);
    }

    public static ScheduledTask delay(Entity entity, Runnable runnable, long delay) {
        return framework().getScheduler().delay(entity, runnable, delay);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long period) {
        return framework().getScheduler().plugin(plugin).repeat(entity, consumer, period, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Runnable runnable, long period) {
        return framework().getScheduler().plugin(plugin).repeat(entity, runnable, period, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return framework().getScheduler().plugin(plugin).repeat(entity, consumer, delay, period);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period) {
        return framework().getScheduler().plugin(plugin).repeat(entity, runnable, delay, period);
    }

    public static ScheduledTask repeat(Entity entity, Consumer<ScheduledUnit> consumer, long period) {
        return framework().getScheduler().repeat(entity, consumer, period, period);
    }

    public static ScheduledTask repeat(Entity entity, Runnable runnable, long period) {
        return framework().getScheduler().repeat(entity, runnable, period, period);
    }

    public static ScheduledTask repeat(Entity entity, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return framework().getScheduler().repeat(entity, consumer, delay, period);
    }

    public static ScheduledTask repeat(Entity entity, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(entity, runnable, delay, period);
    }

}