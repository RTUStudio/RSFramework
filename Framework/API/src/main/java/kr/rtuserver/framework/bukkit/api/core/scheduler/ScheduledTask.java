package kr.rtuserver.framework.bukkit.api.core.scheduler;

import org.bukkit.plugin.Plugin;

public interface ScheduledTask {

    Plugin getPlugin();

    boolean isCancelled();

    void cancel();

}
