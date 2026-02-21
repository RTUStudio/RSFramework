package kr.rtustudio.framework.bukkit.core.scheduler;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledUnit;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

@RequiredArgsConstructor
public final class Scheduler implements kr.rtustudio.framework.bukkit.api.core.scheduler.Scheduler {
    private final RSPlugin plugin;

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask plugin(RSPlugin plugin) {
        return new ScheduledTask(plugin);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Consumer<ScheduledUnit> consumer) {
        return new ScheduledTask(this.plugin).sync(consumer);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(Runnable runnable) {
        return new ScheduledTask(this.plugin).sync(runnable);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask async(
            Consumer<ScheduledUnit> consumer) {
        return new ScheduledTask(this.plugin).async(consumer);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask async(Runnable runnable) {
        return new ScheduledTask(this.plugin).async(runnable);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Consumer<ScheduledUnit> consumer, long delay, boolean async) {
        return new ScheduledTask(this.plugin).delay(consumer, delay, async);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Runnable runnable, long delay, boolean async) {
        return new ScheduledTask(this.plugin).delay(runnable, delay, async);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Consumer<ScheduledUnit> consumer, long delay, long period, boolean async) {
        return new ScheduledTask(this.plugin).repeat(consumer, delay, period, async);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Runnable runnable, long delay, long period, boolean async) {
        return new ScheduledTask(this.plugin).repeat(runnable, delay, period, async);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Location location, Consumer<ScheduledUnit> consumer) {
        return new ScheduledTask(this.plugin).sync(location, consumer);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Location location, Runnable runnable) {
        return new ScheduledTask(this.plugin).sync(location, runnable);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Location location, Consumer<ScheduledUnit> consumer, long delay) {
        return new ScheduledTask(this.plugin).delay(location, consumer, delay);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Location location, Runnable runnable, long delay) {
        return new ScheduledTask(this.plugin).delay(location, runnable, delay);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Location location, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return new ScheduledTask(this.plugin).repeat(location, consumer, delay, period);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Location location, Runnable runnable, long delay, long period) {
        return new ScheduledTask(this.plugin).repeat(location, runnable, delay, period);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Entity entity, Consumer<ScheduledUnit> consumer) {
        return new ScheduledTask(this.plugin).sync(entity, consumer);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Entity entity, Runnable runnable) {
        return new ScheduledTask(this.plugin).sync(entity, runnable);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Entity entity, Consumer<ScheduledUnit> consumer, long delay) {
        return new ScheduledTask(this.plugin).delay(entity, consumer, delay);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Entity entity, Runnable runnable, long delay) {
        return new ScheduledTask(this.plugin).delay(entity, runnable, delay);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Entity entity, Consumer<ScheduledUnit> consumer, long delay, long period) {
        return new ScheduledTask(this.plugin).repeat(entity, consumer, delay, period);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Entity entity, Runnable runnable, long delay, long period) {
        return new ScheduledTask(this.plugin).repeat(entity, runnable, delay, period);
    }
}
