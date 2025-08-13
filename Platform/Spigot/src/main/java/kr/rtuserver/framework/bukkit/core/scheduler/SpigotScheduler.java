package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtuserver.framework.bukkit.api.core.scheduler.Scheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class SpigotScheduler implements Scheduler {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();

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
        this.scheduler.runTask(plugin, task -> consumer.accept(new SpigotTask(task)));
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Runnable runnable) {
        return new SpigotTask(this.scheduler.runTask(plugin, runnable));
    }

    @Override
    public void delay(Plugin plugin, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        if (async) {
            this.scheduler.runTaskLaterAsynchronously(plugin, task -> consumer.accept(new SpigotTask(task)), delay);
        } else {
            this.scheduler.runTaskLater(plugin, task -> consumer.accept(new SpigotTask(task)), delay);
        }
    }

    @Override
    public ScheduledTask delay(Plugin plugin, Runnable runnable, long delay, boolean async) {
        return new SpigotTask(async
                ? this.scheduler.runTaskLaterAsynchronously(plugin, runnable, delay)
                : this.scheduler.runTaskLater(plugin, runnable, delay));
    }

    @Override
    public void repeat(Plugin plugin, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        if (async) {
            this.scheduler.runTaskTimerAsynchronously(plugin, task -> consumer.accept(new SpigotTask(task)), delay, period);
        } else {
            this.scheduler.runTaskTimer(plugin, task -> consumer.accept(new SpigotTask(task)), delay, period);
        }
    }

    @Override
    public ScheduledTask repeat(Plugin plugin, Runnable runnable, long delay, long period, boolean async) {
        return new SpigotTask(async
                ? this.scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period)
                : this.scheduler.runTaskTimer(plugin, runnable, delay, period));
    }

    @Override
    public void async(Plugin plugin, Consumer<ScheduledTask> consumer) {
        this.scheduler.runTaskAsynchronously(plugin, task -> consumer.accept(new SpigotTask(task)));
    }

    @Override
    public ScheduledTask async(Plugin plugin, Runnable runnable) {
        return new SpigotTask(this.scheduler.runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public void sync(Plugin plugin, Location location, Consumer<ScheduledTask> consumer) {
        if (isValid(location)) sync(plugin, consumer);
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Location location, Runnable runnable) {
        if (isValid(location)) return sync(plugin, runnable);
        return null;
    }

    @Override
    public void delay(Plugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        if (isValid(location)) delay(plugin, consumer, delay, async);
    }

    @Override
    public ScheduledTask delay(Plugin plugin, Location location, Runnable runnable, long delay, boolean async) {
        if (isValid(location)) return delay(plugin, runnable, delay, async);
        return null;
    }

    @Override
    public void repeat(Plugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        if (isValid(location)) repeat(plugin, consumer, delay, period, async);
    }

    @Override
    public ScheduledTask repeat(Plugin plugin, Location location, Runnable runnable, long delay, long period, boolean async) {
        if (isValid(location)) return repeat(plugin, runnable, delay, period, async);
        return null;
    }

    @Override
    public void sync(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer) {
        if (isValid(entity)) sync(plugin, consumer);
    }

    @Override
    public ScheduledTask sync(Plugin plugin, Entity entity, Runnable runnable) {
        if (isValid(entity)) return sync(plugin, runnable);
        return null;
    }

    @Override
    public void delay(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, boolean async) {
        if (isValid(entity)) delay(plugin, consumer, delay, async);
    }

    @Override
    public ScheduledTask delay(Plugin plugin, Entity entity, Runnable runnable, long delay, boolean async) {
        if (isValid(entity)) return delay(plugin, runnable, delay, async);
        return null;
    }

    @Override
    public void repeat(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period, boolean async) {
        if (isValid(entity)) repeat(plugin, consumer, delay, period, async);
    }

    @Override
    public ScheduledTask repeat(Plugin plugin, Entity entity, Runnable runnable, long delay, long period, boolean async) {
        if (isValid(entity)) return repeat(plugin, runnable, delay, period, async);
        return null;
    }

}
