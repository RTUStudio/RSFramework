package kr.rtuserver.framework.bukkit.api.storage;

import com.google.gson.JsonObject;
import kr.rtuserver.framework.bukkit.api.platform.JSON;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    CompletableFuture<Boolean> add(@NotNull String name, @NotNull JsonObject data);

    default CompletableFuture<Boolean> add(@NotNull String name, @NotNull JSON data) {
        return add(name, data.get());
    }

    CompletableFuture<Boolean> set(@NotNull String name, @NotNull JsonObject find, @NotNull JsonObject data);

    default CompletableFuture<Boolean> set(@NotNull String name, @NotNull JSON find, @NotNull JSON data) {
        return set(name, find.get(), data.get());
    }

    CompletableFuture<List<JsonObject>> get(@NotNull String name, @NotNull JsonObject find);

    default CompletableFuture<List<JsonObject>> get(@NotNull String name, @NotNull JSON find) {
        return get(name, find.get());
    }

    void close();

}
