package kr.rtustudio.framework.bukkit.core.scheduler;

import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledUnit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public record SpigotUnit(BukkitTask task) implements ScheduledUnit {

    @Override
    public Plugin getPlugin() {
        return task.getOwner();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}
