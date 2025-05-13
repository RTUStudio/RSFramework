package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@Getter
@RequiredArgsConstructor
public class FoliaTask implements ScheduledTask {

    private final io.papermc.paper.threadedregions.scheduler.ScheduledTask task;

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
