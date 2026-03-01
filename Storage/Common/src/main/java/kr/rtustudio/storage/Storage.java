package kr.rtustudio.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public interface Storage {

    @NotNull
    CompletableFuture<Result> add(@NotNull JsonObject data);

    @NotNull
    default CompletableFuture<Result> add(@NotNull JSON data) {
        return add(data.get());
    }

    @NotNull
    CompletableFuture<Result> set(@NotNull JsonObject find, @NotNull JsonObject data);

    @NotNull
    default CompletableFuture<Result> set(@NotNull JSON find, @NotNull JSON data) {
        return set(find.get(), data.get());
    }

    @NotNull
    CompletableFuture<List<JsonObject>> get(@NotNull JsonObject find);

    @NotNull
    default CompletableFuture<List<JsonObject>> get(@NotNull JSON find) {
        return get(find.get());
    }

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
