package kr.rtuserver.framework.bukkit.api.storage;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    CompletableFuture<Boolean> add(@NotNull String name, @NotNull JsonObject data);

    CompletableFuture<Boolean> set(@NotNull String name, @Nullable JsonObject find, @Nullable JsonObject data);

    CompletableFuture<List<JsonObject>> get(@NotNull String name, @Nullable JsonObject find);

    void close();

}
