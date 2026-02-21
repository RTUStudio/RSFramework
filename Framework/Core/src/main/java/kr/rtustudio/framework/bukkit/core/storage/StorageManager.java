package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.core.configuration.StorageConfiguration;
import kr.rtustudio.framework.bukkit.core.configuration.storage.*;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageManager {
    private final RSPlugin plugin;
    private final StorageConfiguration configuration;
    private final Map<String, Storage> storages = new LinkedHashMap<>();

    public StorageManager(RSPlugin plugin) {
        this.plugin = plugin;
        this.configuration = new StorageConfiguration(plugin);
    }

    public void registerStorage(@NotNull String name, @NotNull StorageType type) {
        configuration.registerStorage(name, type);
        storages.computeIfAbsent(name, k -> createStorage(k, configuration.getStorageMap().get(k)));
    }

    @Nullable
    public Storage getStorage(@NotNull String name) {
        return storages.get(name);
    }

    public void reload() {
        configuration.reload();
        configuration
                .getStorageMap()
                .forEach(
                        (name, type) -> {
                            if (configuration.getConfig(type).isChanged()
                                    || !storages.containsKey(name)) {
                                Optional.ofNullable(storages.remove(name))
                                        .ifPresent(Storage::close);
                                storages.put(name, createStorage(name, type));
                            }
                        });
    }

    public void close() {
        storages.values().forEach(Storage::close);
        storages.clear();
    }

    private Storage createStorage(String name, StorageType type) {
        RSConfiguration.Wrapper<?> config = configuration.getConfig(type);
        Storage storage =
                switch (type) {
                    case JSON -> new kr.rtustudio.storage.json.Json((Json) config);
                    case SQLITE -> {
                        SQLite cfg = (SQLite) config;
                        File parent = new File(cfg.getFilePath()).getParentFile();
                        if (parent != null) parent.mkdirs();
                        yield new kr.rtustudio.storage.sqlite.SQLite(cfg, List.of(name));
                    }
                    case MYSQL ->
                            new kr.rtustudio.storage.mysql.MySQL((MySQL) config, List.of(name));
                    case MARIADB ->
                            new kr.rtustudio.storage.mariadb.MariaDB(
                                    (MariaDB) config, List.of(name));
                    case MONGODB -> new kr.rtustudio.storage.mongodb.MongoDB((MongoDB) config);
                    case POSTGRESQL ->
                            new kr.rtustudio.storage.postgresql.PostgreSQL(
                                    (PostgreSQL) config, List.of(name));
                };
        plugin.console("Storage [" + name + "]: " + type.getName());
        return storage;
    }
}
