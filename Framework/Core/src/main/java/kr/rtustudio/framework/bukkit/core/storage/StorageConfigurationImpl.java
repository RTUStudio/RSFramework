package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.StorageConfiguration;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;
import kr.rtustudio.storage.json.Json;
import kr.rtustudio.storage.mariadb.MariaDB;
import kr.rtustudio.storage.mongodb.MongoDB;
import kr.rtustudio.storage.mysql.MySQL;
import kr.rtustudio.storage.postgresql.PostgreSQL;
import kr.rtustudio.storage.sqlite.SQLite;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link StorageConfiguration}의 Core 구현체.
 *
 * <p>{@code Config/Storage.yml}에서 storage name → {@link StorageType} 매핑을 읽고, {@code switch}문으로 해당
 * 타입의 {@link Storage} 인스턴스를 생성·관리한다.
 *
 * <p>각 {@link StorageType}에 대응하는 설정 파일은 {@code Config/Storage/} 하위에 위치한다. (예: {@code Json.yml},
 * {@code MySQL.yml}, {@code SQLite.yml} 등)
 *
 * @see StorageConfiguration
 * @see StorageType
 */
@Slf4j
@SuppressWarnings("unused")
public class StorageConfigurationImpl extends StorageConfiguration {

    /** storage name → StorageType 매핑 (Storage.yml 기반) */
    private final Map<String, StorageType> storageMap = new LinkedHashMap<>();

    /** storage name → 실제 Storage 인스턴스 */
    private final Map<String, Storage> storages = new LinkedHashMap<>();

    /** StorageType → 해당 타입의 Config 객체 (타입당 하나만 생성) */
    private final Map<StorageType, Object> configs = new EnumMap<>(StorageType.class);

    /**
     * @param plugin 이 설정을 소유하는 플러그인
     */
    public StorageConfigurationImpl(RSPlugin plugin) {
        super(plugin);
    }

    /**
     * {@code Config/Storage.yml}에서 기존에 등록된 name-type 매핑을 로드한다.
     *
     * <p>{@link kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration.Wrapper}의 {@code
     * setup()} 호출 시 리플렉션으로 자동 실행된다.
     */
    private void init() {
        for (String key : keys()) {
            String value = getString(key, StorageType.JSON.name());
            storageMap.put(key, StorageType.get(value));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>yml에 name이 없으면 기본 type으로 추가 후 저장하고, 이미 존재하면 yml에 기록된 타입을 우선 사용하여 Storage를 생성한다.
     */
    @Override
    public void registerStorage(@NotNull String name, @NotNull StorageType type) {
        if (!storageMap.containsKey(name)) {
            storageMap.put(name, type);
            set(name, type.name());
            save();
        }
        StorageType resolvedType = storageMap.get(name);
        initConfig(resolvedType);
        createStorage(name, resolvedType);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Storage getStorage(@NotNull String name) {
        return storages.get(name);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public Map<String, StorageType> getStorageMap() {
        return Collections.unmodifiableMap(storageMap);
    }

    /** yml을 다시 로드하고, 모든 Config를 리로드한 뒤, 등록된 스토리지를 설정 변경에 따라 재생성한다. */
    @Override
    public void reload() {
        super.reload();
        reloadConfigs();
        List<String> names = new ArrayList<>(storages.keySet());
        for (String name : names) {
            StorageType type = storageMap.get(name);
            if (type != null) {
                createStorage(name, type);
            }
        }
    }

    /**
     * 등록된 모든 {@link Storage}를 닫고 내부 맵을 비운다.
     *
     * <p>플러그인 비활성화 시 {@code Framework.closeStorages()}에서 호출된다.
     */
    public void closeAll() {
        storages.values().forEach(Storage::close);
        storages.clear();
    }

    /**
     * 주어진 {@link StorageType}에 대응하는 Config 객체를 초기화한다.
     *
     * <p>이미 초기화된 타입은 건너뛴다. {@code switch}문으로 타입별 Config를 생성한다.
     *
     * @param type 초기화할 스토리지 타입
     */
    private void initConfig(StorageType type) {
        if (configs.containsKey(type)) return;
        RSPlugin plugin = getPlugin();
        switch (type) {
            case JSON -> configs.put(type, new JsonConfig(plugin));
            case SQLITE -> configs.put(type, new SQLiteConfig(plugin));
            case MYSQL -> configs.put(type, new MySQLConfig(plugin));
            case MARIADB -> configs.put(type, new MariaDBConfig(plugin));
            case MONGODB -> configs.put(type, new MongoDBConfig(plugin));
            case POSTGRESQL -> configs.put(type, new PostgreSQLConfig(plugin));
        }
    }

    /** 초기화된 모든 Config 객체의 yml을 리로드한다. */
    private void reloadConfigs() {
        for (Object config : configs.values()) {
            if (config
                    instanceof
                    kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration.Wrapper<?>
                                    wrapper) {
                wrapper.reload();
            }
        }
    }

    /**
     * 주어진 name과 type으로 {@link Storage} 인스턴스를 생성하거나 교체한다.
     *
     * <p>기존 인스턴스와 타입이 같고 설정이 변경되지 않았으면 재생성하지 않는다. 설정이 변경되었거나 타입이 다르면 기존 인스턴스를 닫고 새로 생성한다.
     *
     * @param name 스토리지 식별 이름
     * @param type 스토리지 타입
     */
    private void createStorage(String name, StorageType type) {
        Storage existing = storages.get(name);
        switch (type) {
            case JSON -> {
                JsonConfig cfg = (JsonConfig) configs.get(type);
                if (!(existing instanceof Json) || cfg.isChanged()) {
                    if (existing != null) existing.close();
                    storages.put(name, new Json(cfg));
                    getPlugin().console("Storage [" + name + "]: Json");
                }
            }
            case SQLITE -> {
                SQLiteConfig cfg = (SQLiteConfig) configs.get(type);
                if (!(existing instanceof SQLite) || cfg.isChanged()) {
                    if (existing != null) existing.close();
                    ensureParentDir(cfg.getFilePath());
                    storages.put(name, new SQLite(cfg, List.of(name)));
                    getPlugin().console("Storage [" + name + "]: SQLite");
                }
            }
            case MYSQL -> {
                MySQLConfig cfg = (MySQLConfig) configs.get(type);
                if (!(existing instanceof MySQL) || cfg.isChanged()) {
                    if (existing != null) existing.close();
                    storages.put(name, new MySQL(cfg, List.of(name)));
                    getPlugin().console("Storage [" + name + "]: MySQL");
                }
            }
            case MARIADB -> {
                MariaDBConfig cfg = (MariaDBConfig) configs.get(type);
                if (!(existing instanceof MariaDB) || cfg.isChanged()) {
                    if (existing != null) existing.close();
                    storages.put(name, new MariaDB(cfg, List.of(name)));
                    getPlugin().console("Storage [" + name + "]: MariaDB");
                }
            }
            case MONGODB -> {
                MongoDBConfig cfg = (MongoDBConfig) configs.get(type);
                if (!(existing instanceof MongoDB) || cfg.isChanged()) {
                    if (existing != null) existing.close();
                    storages.put(name, new MongoDB(cfg));
                    getPlugin().console("Storage [" + name + "]: MongoDB");
                }
            }
            case POSTGRESQL -> {
                PostgreSQLConfig cfg = (PostgreSQLConfig) configs.get(type);
                if (!(existing instanceof PostgreSQL) || cfg.isChanged()) {
                    if (existing != null) existing.close();
                    storages.put(name, new PostgreSQL(cfg, List.of(name)));
                    getPlugin().console("Storage [" + name + "]: PostgreSQL");
                }
            }
        }
    }

    /**
     * 파일의 부모 디렉토리가 존재하지 않으면 생성한다.
     *
     * @param filePath 파일 경로
     */
    private void ensureParentDir(String filePath) {
        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }
}
