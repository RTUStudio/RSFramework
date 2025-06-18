package kr.rtuserver.framework.bukkit.api.storage.json;

import com.google.common.io.Files;
import com.google.gson.*;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtuserver.framework.bukkit.api.scheduler.CraftScheduler;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class Json implements Storage {

    private final RSPlugin plugin;

    private final Map<String, JsonFile> map = new HashMap<>();

    public Json(RSPlugin plugin, File[] files) {
        this.plugin = plugin;
        JsonConfig config = plugin.getConfigurations().getStorage().getJson();
        for (File file : files) {
            try {
                String name = Files.getNameWithoutExtension(file.getName());
                JsonElement json = JsonParser.parseReader(new FileReader(file));
                map.put(name, new JsonFile(plugin, file, json != null && !json.isJsonNull() ? json.getAsJsonArray() : new JsonArray(),
                        config.getSavePeriod()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CompletableFuture<Boolean> add(@NotNull String name, @NotNull JsonObject data) {
        return CompletableFuture.supplyAsync(() -> {
            if (data.isJsonNull()) return false;
            if (!map.containsKey(name)) {
                plugin.console("<red>Can't load " + name + " data!</red>");
                plugin.console("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>");
                return false;
            }
            return map.get(name).add(data);
        });
    }

    @Override
    public CompletableFuture<Boolean> set(@NotNull String name, @Nullable JsonObject find, @Nullable JsonObject data) {
        return CompletableFuture.supplyAsync(() -> {
            if (!map.containsKey(name)) {
                plugin.console("<red>Can't load " + name + " data!</red>");
                plugin.console("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>");
                return false;
            }
            return map.get(name).set(find, data);
        });
    }

    @Override
    public CompletableFuture<List<JsonObject>> get(@NotNull String name, @NotNull JsonObject find) {
        return CompletableFuture.supplyAsync(() -> {
            if (!map.containsKey(name)) {
                plugin.console("<red>Can't load " + name + " data!</red>");
                plugin.console("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>");
                return null;
            }
            return map.get(name).get(find);
        });
    }

    public boolean sync(String name) {
        if (!map.containsKey(name)) {
            plugin.console("<red>Can't load " + name + " data!</red>");
            plugin.console("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>");
            return false;
        }
        return map.get(name).sync();
    }

    public void close() {
        for (JsonFile data : map.values()) data.close();
        map.clear();
    }

    private static class JsonFile {

        private final RSPlugin plugin;

        private final Gson gson = new Gson();
        private final File file;
        @Getter
        private final AtomicBoolean needSave = new AtomicBoolean(false);
        private final ScheduledTask task;
        @Getter
        private JsonArray data;

        protected JsonFile(RSPlugin plugin, File file, JsonArray data, int savePeriod) {
            this.plugin = plugin;
            this.file = file;
            this.data = data;
            this.task = CraftScheduler.runTimerAsync(plugin, () -> {
                if (!needSave.get()) return;
                CraftScheduler.run(plugin, () -> {
                    save();
                    needSave.set(false);
                });
            }, savePeriod, savePeriod);
        }

        private void debug(String type, String collection, String json) {
            plugin.verbose("[Storage] " + type + ": " + collection + " - " + json);
        }

        private boolean isNull(JsonObject json) {
            return json == null || json.isEmpty() || json.isJsonNull();
        }

        public boolean add(JsonObject value) {
            debug("ADD", file.getName(), value.toString());
            if (isNull(value)) return false;
            data.add(value);
            needSave.lazySet(true);
            return true;
        }

        protected boolean set(JsonObject find, JsonObject value) {
            Map<Integer, JsonObject> list = find(find);
            if (list.isEmpty()) return false;
            final JsonArray backup = data;
            List<Integer> toRemove = new ArrayList<>();
            for (int key : list.keySet()) {
                JsonElement resultValue = list.get(key);
                if (resultValue == null || resultValue.isJsonNull()) return false;
                JsonObject valObj = resultValue.getAsJsonObject();
                if (value == null || value.isJsonNull()) toRemove.add(key);
                else {
                    for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
                        String property = entry.getKey();
                        JsonElement element = entry.getValue();
                        if (element.isJsonNull()) valObj.remove(property);
                        if (element.isJsonPrimitive()) {
                            JsonPrimitive primitive = element.getAsJsonPrimitive();
                            if (primitive.isNumber()) valObj.addProperty(property, primitive.getAsNumber());
                            else if (primitive.isBoolean()) valObj.addProperty(property, primitive.getAsBoolean());
                            else if (primitive.isString()) valObj.addProperty(property, primitive.getAsString());
                            else {
                                plugin.console("<red>Unsupported type of data tried to be saved! Only supports JsonElement, Number, Boolean and String</red>");
                                plugin.console("<red>지원하지 않는 타입의 데이터가 저장되려고 했습니다! JsonElement, Number, Boolean, String만 지원합니다</red>");
                                data = backup;
                                return false;
                            }
                        } else valObj.add(property, element);
                        if (data.contains(valObj)) data.set(key, valObj);
                        else data.add(valObj);
                    }
                }
            }
            for (int i = toRemove.size() - 1; i >= 0; i--) data.remove(toRemove.get(i));
            needSave.lazySet(true);
            return true;
        }

        protected List<JsonObject> get(JsonObject find) {
            Map<Integer, JsonObject> list = find(find);
            return new ArrayList<>(list.values());
        }

        public Map<Integer, JsonObject> find(JsonObject find) {
            Map<Integer, JsonObject> result = new HashMap<>();
            if (data == null || data.isEmpty()) return result;

            boolean isNull = isNull(find);
            for (int i = 0; i < data.size(); i++) {
                JsonElement element = data.get(i);
                if (!element.isJsonObject()) continue;
                JsonObject object = element.getAsJsonObject();

                boolean allMatch = true;
                if (!isNull) {
                    for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();
                        JsonElement get = object.get(key);
                        if (value == null) allMatch = false;
                        else if (value.isJsonNull()) allMatch = get.isJsonNull();
                        else if (value instanceof JsonObject vjo && get instanceof JsonObject gjo) {
                            allMatch = vjo.equals(gjo);
                        } else if (value instanceof JsonPrimitive vjp && get instanceof JsonPrimitive gjp) {
                            allMatch = vjp.equals(gjp);
                        }
                        if (!allMatch) break;
                    }
                }
                if (allMatch) result.put(i, object);
            }
            return result;
        }

        public boolean sync() {
            try {
                JsonElement json = JsonParser.parseReader(new FileReader(file));
                data = json != null && !json.isJsonNull() ? json.getAsJsonArray() : new JsonArray();
                return true;
            } catch (FileNotFoundException e) {
                plugin.console("<red>Error when sync " + file.getName() + "!</red>");
                plugin.console("<red> " + file.getName() + " 파일과 동기화 도중 오류가 발생하였습니다!</red>");
                return false;
            }
        }

        private void save() {
            try (Writer writer = new FileWriter(file)) {
                gson.newBuilder().setPrettyPrinting().create().toJson(data, writer);
            } catch (IOException e) {
                plugin.console("<red>Error when saving " + file.getName() + "!</red>");
                plugin.console("<red> " + file.getName() + " 파일을 저장하는 도중 오류가 발생하였습니다!</red>");
            }
        }

        protected void close() {
            task.cancel();
            save();
        }
    }
}
