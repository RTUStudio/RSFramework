package kr.rtuserver.framework.bukkit.api.core.internal.runnable;

import java.util.Map;
import java.util.UUID;

public interface CommandLimit extends Runnable {
    Map<UUID, Integer> getExecuteLimit();
}
