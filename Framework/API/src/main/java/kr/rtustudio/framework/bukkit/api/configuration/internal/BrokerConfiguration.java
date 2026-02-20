package kr.rtustudio.framework.bukkit.api.configuration.internal;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BrokerConfiguration {

    private final RSPlugin plugin;
    private final ObjectOpenHashSet<String> redisAliases = new ObjectOpenHashSet<>();

    public void registerRedis(@NotNull String name) {
        redisAliases.add(name);
        plugin.console("Broker [" + name + "]: REDIS (shared)");
    }

    public boolean hasRedis(@NotNull String name) {
        return redisAliases.contains(name);
    }

    public void reload() {}

    public void closeAll() {
        redisAliases.clear();
    }
}
