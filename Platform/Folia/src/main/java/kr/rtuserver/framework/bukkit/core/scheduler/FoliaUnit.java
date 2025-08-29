package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledUnit;

import org.bukkit.plugin.Plugin;

public record FoliaUnit(io.papermc.paper.threadedregions.scheduler.ScheduledTask task)
        implements ScheduledUnit {

    @Override
    public Plugin getPlugin() {
        return task.getOwningPlugin();
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
