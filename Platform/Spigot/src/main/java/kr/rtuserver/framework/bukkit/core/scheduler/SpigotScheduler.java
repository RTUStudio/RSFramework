package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
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
    public void run(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        scheduler.runTask(plugin, task -> consumer.accept(new SpigotTask(task)));
    }

    @Override
    public ScheduledTask run(RSPlugin plugin, Runnable runnable) {
        return new SpigotTask(scheduler.runTask(plugin, runnable));
    }

    @Override
    public void runLater(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        scheduler.runTaskLater(plugin, task -> consumer.accept(new SpigotTask(task)), delay);
    }

    @Override
    public ScheduledTask runLater(RSPlugin plugin, Runnable runnable, long delay) {
        return new SpigotTask(scheduler.runTaskLater(plugin, runnable, delay));
    }

    @Override
    public void runTimer(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        scheduler.runTaskTimer(plugin, task -> consumer.accept(new SpigotTask(task)), delay, period);
    }

    @Override
    public ScheduledTask runTimer(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return new SpigotTask(scheduler.runTaskTimer(plugin, runnable, delay, period));
    }

    @Override
    public void runAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer) {
        scheduler.runTaskAsynchronously(plugin, task -> consumer.accept(new SpigotTask(task)));
    }

    @Override
    public ScheduledTask runAsync(RSPlugin plugin, Runnable runnable) {
        return new SpigotTask(scheduler.runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public void runLaterAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay) {
        scheduler.runTaskLaterAsynchronously(plugin, task -> consumer.accept(new SpigotTask(task)), delay);
    }

    @Override
    public ScheduledTask runLaterAsync(RSPlugin plugin, Runnable runnable, long delay) {
        return new SpigotTask(scheduler.runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    @Override
    public void runTimerAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period) {
        scheduler.runTaskTimerAsynchronously(plugin, task -> consumer.accept(new SpigotTask(task)), delay, period);
    }

    @Override
    public ScheduledTask runTimerAsync(RSPlugin plugin, Runnable runnable, long delay, long period) {
        return new SpigotTask(scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    @Override
    public void run(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer) {
        if (isValid(location)) run(plugin, consumer);
    }

    @Override
    public ScheduledTask run(RSPlugin plugin, Location location, Runnable runnable) {
        if (isValid(location)) return run(plugin, runnable);
        return null;
    }

    @Override
    public void runLater(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay) {
        if (isValid(location)) runLater(plugin, consumer, delay);
    }

    @Override
    public ScheduledTask runLater(RSPlugin plugin, Location location, Runnable runnable, long delay) {
        if (isValid(location)) return runLater(plugin, runnable, delay);
        return null;
    }

    @Override
    public void runTimer(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period) {
        if (isValid(location)) runTimer(plugin, consumer, delay, period);
    }

    @Override
    public ScheduledTask runTimer(RSPlugin plugin, Location location, Runnable runnable, long delay, long period) {
        if (isValid(location)) return runTimer(plugin, runnable, delay, period);
        return null;
    }

    @Override
    public void run(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer) {
        if (isValid(entity)) run(plugin, consumer);
    }

    @Override
    public ScheduledTask run(RSPlugin plugin, Entity entity, Runnable runnable) {
        if (isValid(entity)) return run(plugin, runnable);
        return null;
    }

    @Override
    public void runLater(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay) {
        if (isValid(entity)) runLater(plugin, consumer, delay);
    }

    @Override
    public ScheduledTask runLater(RSPlugin plugin, Entity entity, Runnable runnable, long delay) {
        if (isValid(entity)) return runLater(plugin, runnable, delay);
        return null;
    }

    @Override
    public void runTimer(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period) {
        if (isValid(entity)) runTimer(plugin, consumer, delay, period);
    }

    @Override
    public ScheduledTask runTimer(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period) {
        if (isValid(entity)) return runTimer(plugin, runnable, delay, period);
        return null;
    }

}
