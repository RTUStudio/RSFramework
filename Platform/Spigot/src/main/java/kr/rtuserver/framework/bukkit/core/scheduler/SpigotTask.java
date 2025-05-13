package kr.rtuserver.framework.bukkit.core.scheduler;

import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@Getter
@RequiredArgsConstructor
public class SpigotTask implements ScheduledTask {

    private final BukkitTask task;

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
