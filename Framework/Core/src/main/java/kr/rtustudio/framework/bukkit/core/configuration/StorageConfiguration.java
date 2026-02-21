package kr.rtustudio.framework.bukkit.core.configuration;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.core.configuration.storage.*;
import kr.rtustudio.storage.StorageType;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

@Slf4j
@SuppressWarnings("unused")
public class StorageConfiguration extends RSConfiguration.Wrapper<RSPlugin> {
    private final Map<String, StorageType> storageMap = new LinkedHashMap<>();
    private final Map<StorageType, RSConfiguration.Wrapper<?>> configs =
            new EnumMap<>(StorageType.class);

    public StorageConfiguration(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage"));
        setup(this);
    }

    private void init() {
        keys().forEach(
                        key ->
                                storageMap.put(
                                        key,
                                        StorageType.get(getString(key, StorageType.JSON.name()))));
        if (!storageMap.isEmpty()) loadConfigs();
    }

    public void registerStorage(@NotNull String name, @NotNull StorageType type) {
        if (storageMap.putIfAbsent(name, type) == null) {
            set(name, type.name());
            save();
        }
        loadConfigs();
    }

    private void loadConfigs() {
        if (!configs.isEmpty()) return;
        RSPlugin plugin = getPlugin();
        configs.put(StorageType.JSON, new Json(plugin));
        configs.put(StorageType.SQLITE, new SQLite(plugin));
        configs.put(StorageType.MYSQL, new MySQL(plugin));
        configs.put(StorageType.MARIADB, new MariaDB(plugin));
        configs.put(StorageType.MONGODB, new MongoDB(plugin));
        configs.put(StorageType.POSTGRESQL, new PostgreSQL(plugin));
    }

    @NotNull
    public Map<String, StorageType> getStorageMap() {
        return Collections.unmodifiableMap(storageMap);
    }

    public RSConfiguration.Wrapper<?> getConfig(StorageType type) {
        return configs.get(type);
    }

    @Override
    public void reload() {
        super.reload();
        configs.values().forEach(RSConfiguration.Wrapper::reload);
    }
}
