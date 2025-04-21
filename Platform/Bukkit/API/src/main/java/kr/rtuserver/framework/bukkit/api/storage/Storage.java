package kr.rtuserver.framework.bukkit.api.storage;

import com.google.gson.JsonObject;
import kr.rtuserver.framework.bukkit.api.platform.JSON;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    CompletableFuture<Boolean> add(@NotNull String name, @NotNull JsonObject data);

    default CompletableFuture<Boolean> add(@NotNull String name, @NotNull JSON data) {
        return add(name, data.get());
    }

    CompletableFuture<Boolean> set(@NotNull String name, @Nullable Pair<String, Object> find, @Nullable JsonObject data);

    default CompletableFuture<Boolean> set(@NotNull String name, @Nullable Pair<String, Object> find, @NotNull JSON data) {
        return set(name, find, data.get());
    }

    CompletableFuture<List<JsonObject>> get(@NotNull String name, @Nullable Pair<String, Object> find);

    void close();

}
