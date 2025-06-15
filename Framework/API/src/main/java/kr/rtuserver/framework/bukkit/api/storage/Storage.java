package kr.rtuserver.framework.bukkit.api.storage;

import com.google.gson.JsonObject;
import kr.rtuserver.framework.bukkit.api.platform.JSON;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    CompletableFuture<Boolean> add(@NotNull String name, @NotNull JsonObject data);

    default CompletableFuture<Boolean> add(@NotNull String name, @NotNull JSON data) {
        return add(name, data.get());
    }

    CompletableFuture<Boolean> set(@NotNull String name, @Nullable JsonObject find, @Nullable JsonObject data);

    default CompletableFuture<Boolean> set(@NotNull String name, @Nullable JsonObject find, @Nullable JSON data) {
        return set(name, find, data != null ? data.get() : null);
    }

    default CompletableFuture<Boolean> set(@NotNull String name, @Nullable JSON find, @Nullable JsonObject data) {
        return set(name, find != null ? find.get() : null, data);
    }

    default CompletableFuture<Boolean> set(@NotNull String name, @Nullable JSON find, @Nullable JSON data) {
        return set(name, find != null ? find.get() : null, data != null ? data.get() : null);
    }

    CompletableFuture<List<JsonObject>> get(@NotNull String name, @Nullable JsonObject find);

    default CompletableFuture<List<JsonObject>> get(@NotNull String name, @Nullable JSON find) {
        return get(name, find != null ? find.get() : null);
    }

    void close();

}
