package kr.rtuserver.lib.bukkit.api.storage.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.rtuserver.lib.bukkit.api.RSPlugin;
import kr.rtuserver.lib.bukkit.api.storage.Storage;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PostgreSQL implements Storage {

    private final RSPlugin plugin;

    private final Gson gson = new Gson();

    public PostgreSQL(RSPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean add(@NotNull String name, @NotNull JsonObject data) {
        return false;
    }

    @Override
    public boolean set(@NotNull String name, Pair<String, Object> find, Pair<String, Object> data) {
        return false;
    }

    @Override
    public List<JsonObject> get(@NotNull String name, Pair<String, Object> find) {
        return null;
    }

    @Override
    public void close() {

    }
}
