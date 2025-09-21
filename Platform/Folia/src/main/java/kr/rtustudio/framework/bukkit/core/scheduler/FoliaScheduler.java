package kr.rtustudio.framework.bukkit.core.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import kr.rtustudio.framework.bukkit.api.core.scheduler.BukkitScheduler;
import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;

@Getter
@RequiredArgsConstructor
public class FoliaScheduler implements BukkitScheduler {

    private final GlobalRegionScheduler global = Bukkit.getGlobalRegionScheduler();
    private final RegionScheduler region = Bukkit.getRegionScheduler();
    private final AsyncScheduler async = Bukkit.getAsyncScheduler();

    private static ScheduledTask noop() {
        return NoopScheduledTask.INSTANCE;
    }

    private boolean isValid(Location location) {
        if (location == null) return false;
        return location.getWorld() != null;
    }

    private boolean isValid(Entity entity) {
        if (entity == null) return false;
        if (entity.isValid()) {
            if (entity instanceof Player player) return player.isOnline();
            if (entity instanceof Projectile projectile) return !projectile.isDead();
        }
        return false;
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Consumer<ScheduledUnit> consumer) {
        HandleScheduledTask h = new HandleScheduledTask();
        this.global.run(
                plugin,
                task -> {
                    h.set(task);
                    consumer.accept(new FoliaUnit(task));
                });
        return h;
    }

    @Override
    public ScheduledTask async(Plugin plugin, Consumer<ScheduledUnit> consumer) {
        HandleScheduledTask h = new HandleScheduledTask();
        this.async.runNow(
                plugin,
                task -> {
                    h.set(task);
                    consumer.accept(new FoliaUnit(task));
                });
        return h;
    }

    @Override
    public ScheduledTask delay(
            Plugin plugin, Consumer<ScheduledUnit> consumer, long delay, boolean async) {
        HandleScheduledTask h = new HandleScheduledTask();
        if (async) {
            this.async.runDelayed(
                    plugin,
                    task -> {
                        h.set(task);
                        consumer.accept(new FoliaUnit(task));
                    },
                    delay * 50,
                    TimeUnit.MILLISECONDS);
        } else {
            this.global.runDelayed(
                    plugin,
                    task -> {
                        h.set(task);
                        consumer.accept(new FoliaUnit(task));
                    },
                    delay);
        }
        return h;
    }

    @Override
    public ScheduledTask repeat(
            Plugin plugin,
            Consumer<ScheduledUnit> consumer,
            long delay,
            long period,
            boolean async) {
        HandleScheduledTask h = new HandleScheduledTask();
        if (async) {
            this.async.runAtFixedRate(
                    plugin,
                    task -> {
                        h.set(task);
                        consumer.accept(new FoliaUnit(task));
                    },
                    delay * 50,
                    period * 50,
                    TimeUnit.MILLISECONDS);
        } else {
            this.global.runAtFixedRate(
                    plugin,
                    task -> {
                        h.set(task);
                        consumer.accept(new FoliaUnit(task));
                    },
                    delay,
                    period);
        }
        return h;
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Location location, Consumer<ScheduledUnit> consumer) {
        if (!isValid(location)) return noop();
        HandleScheduledTask h = new HandleScheduledTask();
        this.region.run(
                plugin,
                location,
                task -> {
                    h.set(task);
                    consumer.accept(new FoliaUnit(task));
                });
        return h;
    }

    @Override
    public ScheduledTask delay(
            Plugin plugin, Location location, Consumer<ScheduledUnit> consumer, long delay) {
        if (!isValid(location)) return noop();
        HandleScheduledTask h = new HandleScheduledTask();
        this.region.runDelayed(
                plugin,
                location,
                task -> {
                    h.set(task);
                    consumer.accept(new FoliaUnit(task));
                },
                delay);
        return h;
    }

    @Override
    public ScheduledTask repeat(
            Plugin plugin,
            Location location,
            Consumer<ScheduledUnit> consumer,
            long delay,
            long period) {
        if (!isValid(location)) return noop();
        HandleScheduledTask h = new HandleScheduledTask();
        this.region.runAtFixedRate(
                plugin,
                location,
                task -> {
                    h.set(task);
                    consumer.accept(new FoliaUnit(task));
                },
                delay,
                period);
        return h;
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer) {
        if (!isValid(entity)) return noop();
        HandleScheduledTask h = new HandleScheduledTask();
        entity.getScheduler()
                .run(
                        plugin,
                        task -> {
                            h.set(task);
                            consumer.accept(new FoliaUnit(task));
                        },
                        null);
        return h;
    }

    @Override
    public ScheduledTask delay(
            Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long delay) {
        if (!isValid(entity)) return noop();
        HandleScheduledTask h = new HandleScheduledTask();
        entity.getScheduler()
                .runDelayed(
                        plugin,
                        task -> {
                            h.set(task);
                            consumer.accept(new FoliaUnit(task));
                        },
                        null,
                        delay);
        return h;
    }

    @Override
    public ScheduledTask repeat(
            Plugin plugin,
            Entity entity,
            Consumer<ScheduledUnit> consumer,
            long delay,
            long period) {
        if (!isValid(entity)) return noop();
        HandleScheduledTask h = new HandleScheduledTask();
        entity.getScheduler()
                .runAtFixedRate(
                        plugin,
                        task -> {
                            h.set(task);
                            consumer.accept(new FoliaUnit(task));
                        },
                        null,
                        delay,
                        period);
        return h;
    }

    private static final class NoopScheduledTask implements ScheduledTask {
        private static final NoopScheduledTask INSTANCE = new NoopScheduledTask();

        @Override
        public ScheduledTask delay(Consumer<ScheduledUnit> task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Runnable task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Consumer<ScheduledUnit> task, long delay, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask delay(Runnable task, long delay, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Consumer<ScheduledUnit> task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Runnable task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(
                Consumer<ScheduledUnit> task, long delay, long period, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Runnable task, long delay, long period, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask sync(Location location, Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Location location, Runnable task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Entity entity, Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Entity entity, Runnable task) {
            return this;
        }

        @Override
        public ScheduledTask delay(Location location, Consumer<ScheduledUnit> task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Location location, Runnable task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Entity entity, Consumer<ScheduledUnit> task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Entity entity, Runnable task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask repeat(
                Location location, Consumer<ScheduledUnit> task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Location location, Runnable task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(
                Entity entity, Consumer<ScheduledUnit> task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Entity entity, Runnable task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask sync(Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Runnable task) {
            return this;
        }

        @Override
        public ScheduledTask async(Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask async(Runnable task) {
            return this;
        }

        @Override
        public void cancel() {
            /* no-op */
        }
    }

    private static final class FoliaUnit implements ScheduledUnit {
        private final io.papermc.paper.threadedregions.scheduler.ScheduledTask handle;

        private FoliaUnit(io.papermc.paper.threadedregions.scheduler.ScheduledTask handle) {
            this.handle = handle;
        }

        @Override
        public Plugin getPlugin() {
            return handle.getOwningPlugin();
        }

        @Override
        public boolean isCancelled() {
            return handle.isCancelled();
        }

        @Override
        public void cancel() {
            handle.cancel();
        }
    }

    private static final class HandleScheduledTask implements ScheduledTask {
        private io.papermc.paper.threadedregions.scheduler.ScheduledTask handle;

        private void set(io.papermc.paper.threadedregions.scheduler.ScheduledTask h) {
            this.handle = h;
        }

        @Override
        public ScheduledTask delay(Consumer<ScheduledUnit> task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Runnable task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Consumer<ScheduledUnit> task, long delay, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask delay(Runnable task, long delay, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Consumer<ScheduledUnit> task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Runnable task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(
                Consumer<ScheduledUnit> task, long delay, long period, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Runnable task, long delay, long period, boolean async) {
            return this;
        }

        @Override
        public ScheduledTask sync(Location location, Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Location location, Runnable task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Entity entity, Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Entity entity, Runnable task) {
            return this;
        }

        @Override
        public ScheduledTask delay(Location location, Consumer<ScheduledUnit> task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Location location, Runnable task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Entity entity, Consumer<ScheduledUnit> task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask delay(Entity entity, Runnable task, long delay) {
            return this;
        }

        @Override
        public ScheduledTask repeat(
                Location location, Consumer<ScheduledUnit> task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Location location, Runnable task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(
                Entity entity, Consumer<ScheduledUnit> task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask repeat(Entity entity, Runnable task, long delay, long period) {
            return this;
        }

        @Override
        public ScheduledTask sync(Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask sync(Runnable task) {
            return this;
        }

        @Override
        public ScheduledTask async(Consumer<ScheduledUnit> task) {
            return this;
        }

        @Override
        public ScheduledTask async(Runnable task) {
            return this;
        }

        @Override
        public void cancel() {
            if (handle != null) handle.cancel();
        }
    }
}
