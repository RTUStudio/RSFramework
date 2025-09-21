package kr.rtustudio.framework.bukkit.api.storage;

import kr.rtustudio.framework.bukkit.api.platform.JSON;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public interface Storage {

    /**
     * Adds a new JSON object to the storage under the given name.
     *
     * @param name The identifier or table/collection name.
     * @param data The JSON object to add.
     * @return A CompletableFuture containing the result of the operation.
     */
    CompletableFuture<Result> add(@NotNull String name, @NotNull JsonObject data);

    /**
     * Adds a new JSON object from a JSON wrapper type. Delegates to the add method with JsonObject.
     *
     * @param name The identifier or table/collection name.
     * @param data The JSON wrapper object to add.
     * @return A CompletableFuture containing the result of the operation.
     */
    default CompletableFuture<Result> add(@NotNull String name, @NotNull JSON data) {
        return add(name, data.get());
    }

    /**
     * Updates or sets a JSON object in the storage based on a filter.
     *
     * @param name The identifier or table/collection name.
     * @param find The JSON object used to find matching entries.
     * @param data The new JSON object to set for matching entries.
     * @return A CompletableFuture containing the result of the operation.
     */
    CompletableFuture<Result> set(
            @NotNull String name, @NotNull JsonObject find, @NotNull JsonObject data);

    /**
     * Updates or sets a JSON object from JSON wrapper types. Delegates to the set method with
     * JsonObject.
     *
     * @param name The identifier or table/collection name.
     * @param find The JSON wrapper object used to find matching entries.
     * @param data The JSON wrapper object containing new data to set.
     * @return A CompletableFuture containing the result of the operation.
     */
    default CompletableFuture<Result> set(
            @NotNull String name, @NotNull JSON find, @NotNull JSON data) {
        return set(name, find.get(), data.get());
    }

    /**
     * Retrieves a list of JSON objects from the storage that match the given filter.
     *
     * @param name The identifier or table/collection name.
     * @param find The JSON object used to filter matching entries.
     * @return A CompletableFuture containing a list of matching JSON objects.
     */
    CompletableFuture<List<JsonObject>> get(@NotNull String name, @NotNull JsonObject find);

    /**
     * Retrieves a list of JSON objects from JSON wrapper types. Delegates to the get method with
     * JsonObject.
     *
     * @param name The identifier or table/collection name.
     * @param find The JSON wrapper object used to filter matching entries.
     * @return A CompletableFuture containing a list of matching JSON objects.
     */
    default CompletableFuture<List<JsonObject>> get(@NotNull String name, @NotNull JSON find) {
        return get(name, find.get());
    }

    /** Closes the storage and releases any resources used. */
    void close();

    /** Enum representing the result of a storage operation. */
    @Getter
    @RequiredArgsConstructor
    public enum Result {

        /** Operation succeeded and the storage was modified. */
        UPDATED(true, true),

        /** Operation failed, storage was not modified. */
        FAILED(false, false),

        /** Operation succeeded but storage was not changed (no updates applied). */
        UNCHANGED(true, false);

        private final boolean success;
        private final boolean changed;
    }
}
