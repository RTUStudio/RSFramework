package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.core.configuration.StorageConfiguration;
import kr.rtustudio.framework.bukkit.core.configuration.storage.*;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;
import kr.rtustudio.storage.json.Json;
import kr.rtustudio.storage.sqlite.SQLite;

import java.io.File;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageManager {
    private final RSPlugin plugin;
    private final StorageConfiguration configuration;
    private final Map<String, Storage> storages = new LinkedHashMap<>();
    private final Map<StorageType, AutoCloseable> connections = new EnumMap<>(StorageType.class);

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
                                closeConnection(type);
                                Optional.ofNullable(storages.remove(name))
                                        .ifPresent(Storage::close);
                                storages.put(name, createStorage(name, type));
                            }
                        });
    }

    public void close() {
        storages.values().forEach(Storage::close);
        storages.clear();
        connections
                .values()
                .forEach(
                        c -> {
                            try {
                                c.close();
                            } catch (Exception e) {
                                plugin.getLogger()
                                        .warning("Failed to close connection: " + e.getMessage());
                            }
                        });
        connections.clear();
    }

    private void closeConnection(StorageType type) {
        AutoCloseable conn = connections.remove(type);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to close connection: " + e.getMessage());
            }
        }
    }

    private Storage createStorage(String name, StorageType type) {
        RSConfiguration.Wrapper<?> config = configuration.getConfig(type);
        Storage storage =
                switch (type) {
                    case JSON -> new Json((kr.rtustudio.framework.bukkit.core.configuration.storage.Json) config, name);
                    case SQLITE -> {
                        kr.rtustudio.framework.bukkit.core.configuration.storage.SQLite cfg =
                                (kr.rtustudio.framework.bukkit.core.configuration.storage.SQLite) config;
                        File parent = new File(cfg.getFilePath()).getParentFile();
                        if (parent != null) parent.mkdirs();
                        yield new SQLite(cfg, name);
                    }
                    case MYSQL -> {
                        MySQL cfg = (MySQL) config;
                        var conn = (kr.rtustudio.storage.mysql.MySQL.Pool)
                                connections.computeIfAbsent(type, k -> new kr.rtustudio.storage.mysql.MySQL.Pool(cfg));
                        yield new kr.rtustudio.storage.mysql.MySQL(conn, cfg, name);
                    }
                    case MARIADB -> {
                        MariaDB cfg = (MariaDB) config;
                        var conn = (kr.rtustudio.storage.mariadb.MariaDB.Pool)
                                connections.computeIfAbsent(type, k -> new kr.rtustudio.storage.mariadb.MariaDB.Pool(cfg));
                        yield new kr.rtustudio.storage.mariadb.MariaDB(conn, cfg, name);
                    }
                    case MONGODB -> {
                        MongoDB cfg = (MongoDB) config;
                        var conn = (kr.rtustudio.storage.mongodb.MongoDB.Pool)
                                connections.computeIfAbsent(type, k -> new kr.rtustudio.storage.mongodb.MongoDB.Pool(cfg));
                        yield new kr.rtustudio.storage.mongodb.MongoDB(conn, cfg, name);
                    }
                    case POSTGRESQL -> {
                        PostgreSQL cfg = (PostgreSQL) config;
                        var conn = (kr.rtustudio.storage.postgresql.PostgreSQL.Pool)
                                connections.computeIfAbsent(type, k -> new kr.rtustudio.storage.postgresql.PostgreSQL.Pool(cfg));
                        yield new kr.rtustudio.storage.postgresql.PostgreSQL(conn, cfg, name);
                    }
                };
        plugin.console("Storage [" + name + "]: " + type.getName());
        return storage;
    }
}
