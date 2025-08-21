package kr.rtuserver.framework.bukkit.api.scheduler;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.core.scheduler.DefaultScheduleChain;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduleChain;
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

    public static ScheduleChain sync(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        return DefaultScheduleChain.startSync(plugin, consumer);
    }

    public static ScheduleChain sync(RSPlugin plugin, Runnable runnable) {
        return DefaultScheduleChain.startSync(plugin, st -> runnable.run());
    }

    public static ScheduleChain delay(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        return DefaultScheduleChain.startDelay(plugin, consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(plugin, runnable, delay, async);
    }

    public static ScheduleChain delay(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        return DefaultScheduleChain.startDelay(plugin, consumer, delay, false);
    }

    public static ScheduledTask delay(RSPlugin plugin, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, runnable, delay, false);
    }

    public static ScheduleChain repeat(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        return DefaultScheduleChain.startRepeat(plugin, consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(plugin, runnable, delay, period, async);
    }

    public static ScheduleChain repeat(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        return DefaultScheduleChain.startRepeat(plugin, consumer, delay, period, false);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, runnable, delay, period, false);
    }

    public static ScheduleChain async(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        return DefaultScheduleChain.startAsync(plugin, consumer);
    }

    public static ScheduleChain async(RSPlugin plugin, Runnable runnable) {
        return DefaultScheduleChain.startAsync(plugin, st -> runnable.run());
    }

    public static ScheduleChain sync(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer) {
        return DefaultScheduleChain.startSync(plugin, location, consumer);
    }

    public static ScheduleChain sync(RSPlugin plugin, Location location, Runnable runnable) {
        return DefaultScheduleChain.startSync(plugin, location, st -> runnable.run());
    }

    public static ScheduleChain delay(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        return DefaultScheduleChain.startDelay(plugin, location, consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Location location, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(plugin, location, runnable, delay, async);
    }

    public static ScheduleChain delay(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay) {
        return DefaultScheduleChain.startDelay(plugin, location, consumer, delay, false);
    }

    public static ScheduledTask delay(RSPlugin plugin, Location location, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, location, runnable, delay, false);
    }

    public static ScheduleChain repeat(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        return DefaultScheduleChain.startRepeat(plugin, location, consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(plugin, location, runnable, delay, period, async);
    }

    public static ScheduleChain repeat(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period) {
        return DefaultScheduleChain.startRepeat(plugin, location, consumer, delay, period, false);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Location location, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, location, runnable, delay, period, false);
    }

    public static ScheduleChain sync(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer) {
        return DefaultScheduleChain.startSync(plugin, entity, consumer);
    }

    public static ScheduleChain sync(RSPlugin plugin, Entity entity, Runnable runnable) {
        return DefaultScheduleChain.startSync(plugin, entity, st -> runnable.run());
    }

    public static ScheduleChain delay(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        return DefaultScheduleChain.startDelay(plugin, entity, consumer, delay, async);
    }

    public static ScheduledTask delay(RSPlugin plugin, Entity entity, Runnable runnable, long delay, boolean async) {
        return framework().getScheduler().delay(plugin, entity, runnable, delay, async);
    }

    public static ScheduleChain delay(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay) {
        return DefaultScheduleChain.startDelay(plugin, entity, consumer, delay, false);
    }

    public static ScheduledTask delay(RSPlugin plugin, Entity entity, Runnable runnable, long delay) {
        return framework().getScheduler().delay(plugin, entity, runnable, delay, false);
    }

    public static ScheduleChain repeat(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        return DefaultScheduleChain.startRepeat(plugin, entity, consumer, delay, period, async);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period, boolean async) {
        return framework().getScheduler().repeat(plugin, entity, runnable, delay, period, async);
    }

    public static ScheduleChain repeat(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period) {
        return DefaultScheduleChain.startRepeat(plugin, entity, consumer, delay, period, false);
    }

    public static ScheduledTask repeat(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period) {
        return framework().getScheduler().repeat(plugin, entity, runnable, delay, period, false);
    }

    // =====================
    // Chainable API factories (non-breaking additions)
    // =====================

    public static ScheduleChain chainSync(RSPlugin plugin, Runnable initialTask) {
        return DefaultScheduleChain.startSync(plugin, st -> initialTask.run());
    }

    public static ScheduleChain chainAsync(RSPlugin plugin, Runnable initialTask) {
        return DefaultScheduleChain.startAsync(plugin, st -> initialTask.run());
    }

    public static ScheduleChain chainSync(RSPlugin plugin, Location location, Runnable initialTask) {
        return DefaultScheduleChain.startSync(plugin, location, st -> initialTask.run());
    }

    public static ScheduleChain chainAsync(RSPlugin plugin, Location location, Runnable initialTask) {
        return DefaultScheduleChain.startAsync(plugin, location, st -> initialTask.run());
    }

    public static ScheduleChain chainSync(RSPlugin plugin, Entity entity, Runnable initialTask) {
        return DefaultScheduleChain.startSync(plugin, entity, st -> initialTask.run());
    }

    public static ScheduleChain chainAsync(RSPlugin plugin, Entity entity, Runnable initialTask) {
        return DefaultScheduleChain.startAsync(plugin, entity, st -> initialTask.run());
    }
}
