package kr.rtuserver.framework.bukkit.core.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtuserver.framework.bukkit.api.core.scheduler.Scheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class FoliaScheduler implements Scheduler {

    private final GlobalRegionScheduler global = Bukkit.getGlobalRegionScheduler();
    private final RegionScheduler region = Bukkit.getRegionScheduler();
    private final AsyncScheduler async = Bukkit.getAsyncScheduler();

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
    public void sync(Plugin plugin, Consumer<ScheduledTask> consumer) {
        this.global.run(plugin, task -> consumer.accept(new FoliaTask(task)));
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Runnable runnable) {
        return new FoliaTask(this.global.run(plugin, scheduledTask -> runnable.run()));
    }

    @Override
    public void delay(Plugin plugin, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        if (async) {
            this.async.runDelayed(plugin, task -> consumer.accept(new FoliaTask(task)), delay * 50, TimeUnit.MILLISECONDS);
        } else {
            this.global.runDelayed(plugin, task -> consumer.accept(new FoliaTask(task)), delay);
        }
    }

    @Override
    public ScheduledTask delay(Plugin plugin, Runnable runnable, long delay, boolean async) {
        return new FoliaTask(async
                ? this.async.runDelayed(plugin, scheduledTask -> runnable.run(), delay * 50, TimeUnit.MILLISECONDS)
                : this.global.runDelayed(plugin, scheduledTask -> runnable.run(), delay));
    }

    @Override
    public void repeat(Plugin plugin, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        if (async) {
            this.async.runAtFixedRate(plugin, task -> consumer.accept(new FoliaTask(task)), delay * 50, period * 50, TimeUnit.MILLISECONDS);
        } else {
            this.global.runAtFixedRate(plugin, task -> consumer.accept(new FoliaTask(task)), delay, period);
        }
    }

    @Override
    public ScheduledTask repeat(Plugin plugin, Runnable runnable, long delay, long period, boolean async) {
        return new FoliaTask(async
                ? this.async.runAtFixedRate(plugin, scheduledTask -> runnable.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS)
                : this.global.runAtFixedRate(plugin, scheduledTask -> runnable.run(), delay, period));
    }

    @Override
    public void async(Plugin plugin, Consumer<ScheduledTask> consumer) {
        this.async.runNow(plugin, task -> consumer.accept(new FoliaTask(task)));
    }

    @Override
    public ScheduledTask async(Plugin plugin, Runnable runnable) {
        return new FoliaTask(this.async.runNow(plugin, scheduledTask -> runnable.run()));
    }

    @Override
    public void sync(Plugin plugin, Location location, Consumer<ScheduledTask> consumer) {
        if (isValid(location)) this.region.run(plugin, location, task -> consumer.accept(new FoliaTask(task)));
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Location location, Runnable runnable) {
        if (isValid(location)) return sync(plugin, runnable);
        return null;
    }

    @Override
    public void delay(Plugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        if (isValid(location)) this.region.runDelayed(plugin, location, task -> consumer.accept(new FoliaTask(task)), delay);
    }

    @Override
    public ScheduledTask delay(Plugin plugin, Location location, Runnable runnable, long delay, boolean async) {
        if (isValid(location)) return delay(plugin, runnable, delay, async);
        return null;
    }

    @Override
    public void repeat(Plugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        if (isValid(location))
            this.region.runAtFixedRate(plugin, location, task -> consumer.accept(new FoliaTask(task)), delay, period);
    }

    @Override
    public ScheduledTask repeat(Plugin plugin, Location location, Runnable runnable, long delay, long period, boolean async) {
        if (isValid(location)) return repeat(plugin, runnable, delay, period, async);
        return null;
    }

    @Override
    public void sync(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer) {
        if (isValid(entity)) entity.getScheduler().run(plugin, task -> consumer.accept(new FoliaTask(task)), null);
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Entity entity, Runnable runnable) {
        if (isValid(entity)) return sync(plugin, runnable);
        return null;
    }

    @Override
    public void delay(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        if (isValid(entity))
            entity.getScheduler().runDelayed(plugin, task -> consumer.accept(new FoliaTask(task)), null, delay);
    }

    @Override
    public ScheduledTask delay(Plugin plugin, Entity entity, Runnable runnable, long delay, boolean async) {
        if (isValid(entity)) return delay(plugin, runnable, delay, async);
        return null;
    }

    @Override
    public void repeat(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        if (isValid(entity))
            entity.getScheduler().runAtFixedRate(plugin, task -> consumer.accept(new FoliaTask(task)), null, delay, period);
    }

    @Override
    public ScheduledTask repeat(Plugin plugin, Entity entity, Runnable runnable, long delay, long period, boolean async) {
        if (isValid(entity)) return repeat(plugin, runnable, delay, period, async);
        return null;
    }

}

