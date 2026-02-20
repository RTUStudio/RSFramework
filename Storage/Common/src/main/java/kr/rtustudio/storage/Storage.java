package kr.rtustudio.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public interface Storage {

    @NotNull
    CompletableFuture<Result> add(@NotNull String name, @NotNull JsonObject data);

    @NotNull
    CompletableFuture<Result> set(
            @NotNull String name, @NotNull JsonObject find, @NotNull JsonObject data);

    @NotNull
    CompletableFuture<List<JsonObject>> get(@NotNull String name, @NotNull JsonObject find);

    void close();

    @Getter
    @RequiredArgsConstructor
    enum Result {
        UPDATED(true, true),

        FAILED(false, false),

        UNCHANGED(true, false);

        private final boolean success;
        private final boolean changed;
    }
}
