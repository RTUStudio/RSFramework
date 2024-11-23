package kr.rtuserver.framework.bukkit.api.storage;

import com.google.gson.JsonObject;
import kr.rtuserver.framework.bukkit.api.utility.platform.JSON;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Storage {

    boolean add(@NotNull String name, @NotNull JsonObject data);

    default boolean add(@NotNull String name, @NotNull JSON data) {
        return add(name, data.get());
    }

    boolean set(@NotNull String name, @Nullable Pair<String, Object> find, @Nullable Pair<String, Object> data);

    List<JsonObject> get(@NotNull String name, @Nullable Pair<String, Object> find);

    void close();
}
