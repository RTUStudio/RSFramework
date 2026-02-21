package kr.rtustudio.framework.bukkit.core.scheduler;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.core.scheduler.BukkitScheduler;
import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledUnit;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class ScheduledTask
        implements kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask {
    private final RSPlugin plugin;
    private final BukkitScheduler scheduler;
    private final Deque<Runnable> steps = new ArrayDeque<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask>
            active = new AtomicReference<>(null);

    ScheduledTask(RSPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        if (MinecraftVersion.isFolia()) scheduler = new FoliaScheduler();
        else if (MinecraftVersion.isPaper()) scheduler = new SpigotScheduler();
        else scheduler = new SpigotScheduler();
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Consumer<ScheduledUnit> task, long delay) {
        return delay(task, delay, false);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Runnable task, long delay) {
        Objects.requireNonNull(task, "task");
        return delay(unit -> task.run(), delay, false);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Consumer<ScheduledUnit> task, long delay, boolean async) {
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleOnce(task, delay, async, this::onStepComplete));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Runnable task, long delay, boolean async) {
        Objects.requireNonNull(task, "task");
        return delay(unit -> task.run(), delay, async);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Consumer<ScheduledUnit> task, long delay, long period) {
        return repeat(task, delay, period, false);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Consumer<ScheduledUnit> task, long delay, long period, boolean async) {
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleRepeat(task, delay, period, async));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Runnable task, long delay, long period) {
        Objects.requireNonNull(task, "task");
        return repeat(unit -> task.run(), delay, period, false);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Runnable task, long delay, long period, boolean async) {
        Objects.requireNonNull(task, "task");
        return repeat(unit -> task.run(), delay, period, async);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Location location, Consumer<ScheduledUnit> task) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleOnceAt(location, task, 0L));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Location location, Runnable task) {
        Objects.requireNonNull(task, "task");
        return sync(location, unit -> task.run());
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Entity entity, Consumer<ScheduledUnit> task) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleOnceAt(entity, task, 0L));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Entity entity, Runnable task) {
        Objects.requireNonNull(task, "task");
        return sync(entity, unit -> task.run());
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Location location, Consumer<ScheduledUnit> task, long delay) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleOnceAt(location, task, delay));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Location location, Runnable task, long delay) {
        Objects.requireNonNull(task, "task");
        return delay(location, unit -> task.run(), delay);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Entity entity, Consumer<ScheduledUnit> task, long delay) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleOnceAt(entity, task, delay));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask delay(
            Entity entity, Runnable task, long delay) {
        Objects.requireNonNull(task, "task");
        return delay(entity, unit -> task.run(), delay);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Location location, Consumer<ScheduledUnit> task, long delay, long period) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleRepeatAt(location, task, delay, period));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Location location, Runnable task, long delay, long period) {
        Objects.requireNonNull(task, "task");
        return repeat(location, unit -> task.run(), delay, period);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Entity entity, Consumer<ScheduledUnit> task, long delay, long period) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleRepeatAt(entity, task, delay, period));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask repeat(
            Entity entity, Runnable task, long delay, long period) {
        Objects.requireNonNull(task, "task");
        return repeat(entity, unit -> task.run(), delay, period);
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(
            Consumer<ScheduledUnit> task) {
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleImmediate(task, false, this::onStepComplete));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask sync(Runnable task) {
        Objects.requireNonNull(task, "task");
        return sync(unit -> task.run());
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask async(
            Consumer<ScheduledUnit> task) {
        Objects.requireNonNull(task, "task");
        enqueue(() -> scheduleImmediate(task, true, this::onStepComplete));
        return this;
    }

    @Override
    public kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask async(Runnable task) {
        Objects.requireNonNull(task, "task");
        return async(unit -> task.run());
    }

    private void enqueue(Runnable step) {
        steps.add(step);
        if (running.compareAndSet(false, true)) {
            playNext();
        }
    }

    private void playNext() {
        if (cancelled.get()) {
            running.set(false);
            return;
        }
        Runnable next = steps.poll();
        if (next != null) {
            running.set(true);
            next.run();
        } else running.set(false);
    }

    private void scheduleOnce(
            Consumer<ScheduledUnit> task, long delay, boolean async, Runnable onComplete) {
        if (cancelled.get()) return;
        Consumer<ScheduledUnit> callback =
                st -> {
                    if (cancelled.get()) return;
                    task.accept(st);
                    onComplete.run();
                };
        long d = Math.max(0L, delay);
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                scheduler.delay(plugin, callback, d, async);
        active.set(handle);
    }

    private void scheduleImmediate(
            Consumer<ScheduledUnit> task, boolean async, Runnable onComplete) {
        if (cancelled.get()) return;
        Consumer<ScheduledUnit> callback =
                st -> {
                    if (cancelled.get()) return;
                    task.accept(st);
                    onComplete.run();
                };
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                async ? scheduler.async(plugin, callback) : scheduler.sync(plugin, callback);
        active.set(handle);
    }

    private void scheduleOnceAt(Location location, Consumer<ScheduledUnit> task, long delay) {
        if (cancelled.get()) return;
        Consumer<ScheduledUnit> callback =
                st -> {
                    if (cancelled.get()) return;
                    task.accept(st);
                    onStepComplete();
                };
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                scheduler.delay(plugin, location, callback, Math.max(0L, delay));
        active.set(handle);
    }

    private void scheduleOnceAt(Entity entity, Consumer<ScheduledUnit> task, long delay) {
        if (cancelled.get()) return;
        Consumer<ScheduledUnit> callback =
                st -> {
                    if (cancelled.get()) return;
                    task.accept(st);
                    onStepComplete();
                };
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                scheduler.delay(plugin, entity, callback, Math.max(0L, delay));
        active.set(handle);
    }

    private void scheduleRepeat(
            Consumer<ScheduledUnit> task, long delay, long period, boolean async) {
        if (cancelled.get()) return;
        AtomicBoolean explicitCancel = new AtomicBoolean(false);
        Consumer<ScheduledUnit> wrapper =
                underlying -> {
                    ScheduledUnit wrapped = new RepeatHandle(underlying, explicitCancel);
                    task.accept(wrapped);
                };
        long d = Math.max(0L, delay);
        long p = Math.max(1L, period);
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                scheduler.repeat(plugin, wrapper, d, p, async);
        active.set(handle);
    }

    private void scheduleRepeatAt(
            Location location, Consumer<ScheduledUnit> task, long delay, long period) {
        if (cancelled.get()) return;
        AtomicBoolean explicitCancel = new AtomicBoolean(false);
        Consumer<ScheduledUnit> wrapper =
                underlying -> {
                    ScheduledUnit wrapped = new RepeatHandle(underlying, explicitCancel);
                    task.accept(wrapped);
                };
        long d = Math.max(0L, delay);
        long p = Math.max(1L, period);
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                scheduler.repeat(plugin, location, wrapper, d, p);
        active.set(handle);
    }

    private void scheduleRepeatAt(
            Entity entity, Consumer<ScheduledUnit> task, long delay, long period) {
        if (cancelled.get()) return;
        AtomicBoolean explicitCancel = new AtomicBoolean(false);
        Consumer<ScheduledUnit> wrapper =
                underlying -> {
                    ScheduledUnit wrapped = new RepeatHandle(underlying, explicitCancel);
                    task.accept(wrapped);
                };
        long d = Math.max(0L, delay);
        long p = Math.max(1L, period);
        kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                scheduler.repeat(plugin, entity, wrapper, d, p);
        active.set(handle);
    }

    private void onStepComplete() {
        active.set(null);
        if (running.compareAndSet(true, false)) {
            playNext();
        }
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            steps.clear();
            kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask handle =
                    active.getAndSet(null);
            if (handle != null)
                try {
                    handle.cancel();
                } catch (Throwable ignored) {
                }
            running.set(false);
        }
    }

    private final class RepeatHandle implements ScheduledUnit {
        private final ScheduledUnit delegate;
        private final AtomicBoolean explicitCancel;

        private RepeatHandle(ScheduledUnit delegate, AtomicBoolean explicitCancel) {
            this.delegate = delegate;
            this.explicitCancel = explicitCancel;
        }

        @Override
        public Plugin getPlugin() {
            return delegate.getPlugin();
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled() || explicitCancel.get();
        }

        @Override
        public void cancel() {
            if (explicitCancel.compareAndSet(false, true)) {
                delegate.cancel();
                if (running.compareAndSet(true, false)) {
                    playNext();
                }
            }
        }
    }
}
