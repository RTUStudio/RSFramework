package kr.rtustudio.storage.json;

import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageLogger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;

import com.google.gson.*;

@Slf4j
public class Json implements Storage {

    private final File dataFolder;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Object> tableLocks = new ConcurrentHashMap<>();

    public Json(Config config) {
        this.dataFolder = new File(config.getDataFolder());
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create data folder: {}", dataFolder.getAbsolutePath());
        }
    }

    private Object getTableLock(String table) {
        return tableLocks.computeIfAbsent(table, k -> new Object());
    }

    private File tableFile(String table) {
        return new File(dataFolder, table + ".json");
    }

    private JsonArray readAll(String table) {
        File file = tableFile(table);
        if (!file.exists()) return new JsonArray();
        try (FileReader reader = new FileReader(file)) {
            JsonElement el = gson.fromJson(reader, JsonElement.class);
            return (el != null && el.isJsonArray()) ? el.getAsJsonArray() : new JsonArray();
        } catch (IOException e) {
            StorageLogger.logError(log, "READ", table, e);
            return new JsonArray();
        }
    }

    private void writeAll(String table, JsonArray array) {
        try (FileWriter writer = new FileWriter(tableFile(table))) {
            gson.toJson(array, writer);
        } catch (IOException e) {
            StorageLogger.logError(log, "WRITE", table, e);
        }
    }

    private boolean isNull(JsonObject json) {
        return json == null || json.isEmpty() || json.isJsonNull();
    }

    private boolean matches(JsonObject record, JsonObject find) {
        if (isNull(find)) return true;
        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
            JsonElement val = record.get(entry.getKey());
            if (val == null || !val.equals(entry.getValue())) return false;
        }
        return true;
    }

    @Override
    public @NonNull CompletableFuture<Result> add(@NonNull String table, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    synchronized (getTableLock(table)) {
                        JsonArray array = readAll(table);
                        array.add(data);
                        writeAll(table, array);
                        StorageLogger.logAdd(log, table, data.toString());
                        return Result.UPDATED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<Result> set(
            @NonNull String table, @NonNull JsonObject find, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    synchronized (getTableLock(table)) {
                        JsonArray array = readAll(table);
                        JsonArray updated = new JsonArray();
                        boolean changed = false;

                        for (JsonElement el : array) {
                            if (!el.isJsonObject()) {
                                updated.add(el);
                                continue;
                            }
                            JsonObject record = el.getAsJsonObject();
                            if (matches(record, find)) {
                                changed = true;
                                if (!isNull(data)) {
                                    for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                                        record.add(entry.getKey(), entry.getValue());
                                    }
                                    updated.add(record);
                                }
                            } else {
                                updated.add(record);
                            }
                        }

                        if (changed) {
                            writeAll(table, updated);
                            StorageLogger.logSet(
                                    log, table, (find != null ? find.toString() : "{}"));
                            return Result.UPDATED;
                        }
                        return Result.UNCHANGED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<List<JsonObject>> get(
            @NonNull String table, @NonNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    synchronized (getTableLock(table)) {
                        List<JsonObject> result = new ArrayList<>();
                        for (JsonElement el : readAll(table)) {
                            if (!el.isJsonObject()) continue;
                            JsonObject record = el.getAsJsonObject();
                            if (matches(record, find)) result.add(record);
                        }
                        StorageLogger.logGet(log, table, (find != null ? find.toString() : "{}"));
                        return result;
                    }
                });
    }

    @Override
    public void close() {
        tableLocks.clear();
    }

    public interface Config {
        String getDataFolder();
    }
}
