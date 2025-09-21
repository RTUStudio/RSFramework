package kr.rtustudio.framework.bukkit.api.core.scheduler;

import org.bukkit.plugin.Plugin;

public interface ScheduledUnit {

    Plugin getPlugin();

    boolean isCancelled();

    void cancel();
}
