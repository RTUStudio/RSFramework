package kr.rtuserver.framework.bukkit.core.internal.runnable;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CommandLimit implements kr.rtuserver.framework.bukkit.api.core.internal.runnable.CommandLimit {

    private final Map<UUID, Integer> executeLimit = new ConcurrentHashMap<>();

    public CommandLimit(RSPlugin plugin) {
        CraftScheduler.repeat(plugin, this, 0, 1, true);
    }

    @Override
    public void run() {
        for (UUID uuid : executeLimit.keySet()) {
            if (executeLimit.get(uuid) > 0) executeLimit.put(uuid, executeLimit.get(uuid) - 1);
            else executeLimit.remove(uuid);
        }
    }

}
