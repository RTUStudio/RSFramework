package kr.rtuserver.framework.bukkit.api.scheduler;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class CraftScheduler {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static void sync(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().sync(plugin, consumer);
    }

    public static ScheduledTask sync(RSPlugin plugin, Runnable runnable) {
        return framework().getScheduler().sync(plugin, runnable);
    }

    public static void delay(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        framework().getScheduler().delay(plugin, consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(plugin, runnable, delay, async);
    }

    public static void delay(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().delay(plugin, consumer, delay, false);
    }

    public static ScheduledTask delay(RSPlugin plugin, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, runnable, delay, false);
    }

    public static void repeat(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        framework().getScheduler().repeat(plugin, consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(plugin, runnable, delay, period, async);
    }

    public static void repeat(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().repeat(plugin, consumer, delay, period, false);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, runnable, delay, period, false);
    }

    public static void async(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().async(plugin, consumer);
    }

    public static ScheduledTask async(RSPlugin plugin, Runnable runnable) {
        return framework().getScheduler().async(plugin, runnable);
    }

    public static void delayAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().delay(plugin, consumer, delay, true);
    }

    public static ScheduledTask delayAsync(RSPlugin plugin, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, runnable, delay, true);
    }

    public static void repeatAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().repeat(plugin, consumer, delay, period, true);
    }

    public static ScheduledTask repeatAsync(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, runnable, delay, period, true);
    }

    public static void sync(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().sync(plugin, location, consumer);
    }

    public static ScheduledTask sync(RSPlugin plugin, Location location, Runnable runnable) {
        return framework().getScheduler().sync(plugin, location, runnable);
    }

    public static void delay(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        framework().getScheduler().delay(plugin, location, consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Location location, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(plugin, location, runnable, delay, async);
    }

    public static void delay(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().delay(plugin, location, consumer, delay, false);
    }

    public static ScheduledTask delay(RSPlugin plugin, Location location, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, location, runnable, delay, false);
    }

    public static void repeat(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        framework().getScheduler().repeat(plugin, location, consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(plugin, location, runnable, delay, period, async);
    }

    public static void repeat(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().repeat(plugin, location, consumer, delay, period, false);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, location, runnable, delay, period, false);
    }

    public static void sync(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().sync(plugin, entity, consumer);
    }

    public static ScheduledTask sync(RSPlugin plugin, Entity entity, Runnable runnable) {
        return framework().getScheduler().sync(plugin, entity, runnable);
    }

    public static void delay(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        framework().getScheduler().delay(plugin, entity, consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Entity entity, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(plugin, entity, runnable, delay, async);
    }

    public static void delay(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().delay(plugin, entity, consumer, delay, false);
    }

    public static ScheduledTask delay(RSPlugin plugin, Entity entity, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, entity, runnable, delay, false);
    }

    public static void repeat(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        framework().getScheduler().repeat(plugin, entity, consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(plugin, entity, runnable, delay, period, async);
    }

    public static void repeat(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().repeat(plugin, entity, consumer, delay, period, false);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, entity, runnable, delay, period, false);
    }
}
