package kr.rtustudio.framework.bukkit.api.core.provider;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public interface Provider {

    @Nullable
    Plugin getPlugin();
}
