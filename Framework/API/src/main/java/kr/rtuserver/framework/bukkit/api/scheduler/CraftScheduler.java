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

    public static void run(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().run(plugin, consumer);
    }

    public static ScheduledTask run(RSPlugin plugin, Runnable runnable) {
        return framework().getScheduler().run(plugin, runnable);
    }

    public static void runLater(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().runLater(plugin, consumer, delay);
    }

    public static ScheduledTask runLater(RSPlugin plugin, Runnable runnable, long delay) {
        return framework().getScheduler().runLater(plugin, runnable, delay);
    }

    public static void runTimer(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().runTimer(plugin, consumer, delay, period);
    }

    public static ScheduledTask runTimer(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return framework().getScheduler().runTimer(plugin, runnable, delay, period);
    }

    public static void runAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().runAsync(plugin, consumer);
    }

    public static ScheduledTask runAsync(RSPlugin plugin, Runnable runnable) {
        return framework().getScheduler().runAsync(plugin, runnable);
    }

    public static void runLaterAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().runLaterAsync(plugin, consumer, delay);
    }

    public static ScheduledTask runLaterAsync(RSPlugin plugin, Runnable runnable, long delay) {
        return framework().getScheduler().runLaterAsync(plugin, runnable, delay);
    }

    public static void runTimerAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().runTimerAsync(plugin, consumer, delay, period);
    }

    public static ScheduledTask runTimerAsync(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return framework().getScheduler().runTimerAsync(plugin, runnable, delay, period);
    }

    public static void run(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().run(plugin, location, consumer);
    }

    public static ScheduledTask run(RSPlugin plugin, Location location, Runnable runnable) {
        return framework().getScheduler().run(plugin, location, runnable);
    }

    public static void runLater(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().runLater(plugin, location, consumer, delay);
    }

    public static ScheduledTask runLater(RSPlugin plugin, Location location, Runnable runnable, long delay) {
        return framework().getScheduler().runLater(plugin, location, runnable, delay);
    }

    public static void runTimer(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().runTimer(plugin, location, consumer, delay, period);
    }

    public static ScheduledTask runTimer(RSPlugin plugin, Location location, Runnable runnable, long delay, long period) {
        return framework().getScheduler().runTimer(plugin, location, runnable, delay, period);
    }

    public static void run(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer) {
        framework().getScheduler().run(plugin, entity, consumer);
    }

    public static ScheduledTask run(RSPlugin plugin, Entity entity, Runnable runnable) {
        return framework().getScheduler().run(plugin, entity, runnable);
    }

    public static void runLater(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay) {
        framework().getScheduler().runLater(plugin, entity, consumer, delay);
    }

    public static ScheduledTask runLater(RSPlugin plugin, Entity entity, Runnable runnable, long delay) {
        return framework().getScheduler().runLater(plugin, entity, runnable, delay);
    }

    public static void runTimer(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period) {
        framework().getScheduler().runTimer(plugin, entity, consumer, delay, period);
    }

    public static ScheduledTask runTimer(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period) {
        return framework().getScheduler().runTimer(plugin, entity, runnable, delay, period);
    }
}
