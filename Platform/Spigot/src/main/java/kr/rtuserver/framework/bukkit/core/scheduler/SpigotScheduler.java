package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.core.scheduler.BukkitScheduler;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@RequiredArgsConstructor
public class SpigotScheduler implements BukkitScheduler {

    private final org.bukkit.scheduler.BukkitScheduler scheduler = Bukkit.getScheduler();

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
        BukkitRunnable r =
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        consumer.accept(new SpigotUnit(plugin, this));
                    }
                };
        r.runTask(plugin);
        return new HandleScheduledTask(r);
    }

    @Override
    public ScheduledTask async(Plugin plugin, Consumer<ScheduledUnit> consumer) {
        BukkitRunnable r =
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        consumer.accept(new SpigotUnit(plugin, this));
                    }
                };
        r.runTaskAsynchronously(plugin);
        return new HandleScheduledTask(r);
    }

    @Override
    public ScheduledTask delay(
            Plugin plugin, Consumer<ScheduledUnit> consumer, long delay, boolean async) {
        if (async) {
            BukkitRunnable r =
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            consumer.accept(new SpigotUnit(plugin, this));
                        }
                    };
            r.runTaskLaterAsynchronously(plugin, delay);
            return new HandleScheduledTask(r);
        } else {
            BukkitRunnable r =
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            consumer.accept(new SpigotUnit(plugin, this));
                        }
                    };
            r.runTaskLater(plugin, delay);
            return new HandleScheduledTask(r);
        }
    }

    @Override
    public ScheduledTask repeat(
            Plugin plugin,
            Consumer<ScheduledUnit> consumer,
            long delay,
            long period,
            boolean async) {
        if (async) {
            BukkitRunnable r =
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            consumer.accept(new SpigotUnit(plugin, this));
                        }
                    };
            r.runTaskTimerAsynchronously(plugin, delay, period);
            return new HandleScheduledTask(r);
        } else {
            BukkitRunnable r =
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            consumer.accept(new SpigotUnit(plugin, this));
                        }
                    };
            r.runTaskTimer(plugin, delay, period);
            return new HandleScheduledTask(r);
        }
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Location location, Consumer<ScheduledUnit> consumer) {
        if (isValid(location)) return sync(plugin, consumer);
        return noop();
    }

    @Override
    public ScheduledTask delay(
            Plugin plugin, Location location, Consumer<ScheduledUnit> consumer, long delay) {
        if (isValid(location)) return delay(plugin, consumer, delay, false);
        return noop();
    }

    @Override
    public ScheduledTask repeat(
            Plugin plugin,
            Location location,
            Consumer<ScheduledUnit> consumer,
            long delay,
            long period) {
        if (isValid(location)) return repeat(plugin, consumer, delay, period, false);
        return noop();
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer) {
        if (isValid(entity)) return sync(plugin, consumer);
        return noop();
    }

    @Override
    public ScheduledTask delay(
            Plugin plugin, Entity entity, Consumer<ScheduledUnit> consumer, long delay) {
        if (isValid(entity)) return delay(plugin, consumer, delay, false);
        return noop();
    }

    @Override
    public ScheduledTask repeat(
            Plugin plugin,
            Entity entity,
            Consumer<ScheduledUnit> consumer,
            long delay,
            long period) {
        if (isValid(entity)) return repeat(plugin, consumer, delay, period, false);
        return noop();
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
            // no-op
        }
    }

    private static final class SpigotUnit implements ScheduledUnit {
        private final Plugin plugin;
        private final BukkitRunnable runnable;

        private SpigotUnit(Plugin plugin, BukkitRunnable runnable) {
            this.plugin = plugin;
            this.runnable = runnable;
        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public boolean isCancelled() {
            return runnable.isCancelled();
        }

        @Override
        public void cancel() {
            runnable.cancel();
        }
    }

    private static final class HandleScheduledTask implements ScheduledTask {
        private final BukkitRunnable runnable;

        private HandleScheduledTask(BukkitRunnable runnable) {
            this.runnable = runnable;
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
            runnable.cancel();
        }
    }
}
