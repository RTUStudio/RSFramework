package kr.rtustudio.bridge;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BridgeRegistry {

    private final Map<Class<? extends Bridge>, Bridge> bridges = new HashMap<>();

    public <T extends Bridge> void register(@NotNull Class<T> type, @NotNull T bridge) {
        bridges.put(type, bridge);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Bridge> T get(@NotNull Class<T> type) {
        return (T) bridges.get(type);
    }

    public boolean has(@NotNull Class<? extends Bridge> type) {
        return bridges.containsKey(type);
    }

    public void closeAll() {
        bridges.values().forEach(Bridge::close);
        bridges.clear();
    }
}
