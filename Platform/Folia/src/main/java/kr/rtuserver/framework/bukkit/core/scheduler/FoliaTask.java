package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public record FoliaTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) implements ScheduledTask {

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
