package kr.rtuserver.framework.bukkit.api.configuration.impl;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import kr.rtuserver.framework.bukkit.api.storage.StorageType;
import kr.rtuserver.framework.bukkit.api.storage.config.*;
import kr.rtuserver.framework.bukkit.api.storage.impl.Json;
import kr.rtuserver.framework.bukkit.api.storage.impl.MariaDB;
import kr.rtuserver.framework.bukkit.api.storage.impl.MongoDB;
import kr.rtuserver.framework.bukkit.api.storage.impl.MySQL;
import kr.rtuserver.framework.bukkit.api.utility.platform.FileResource;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Configurations {

    private final RSPlugin plugin;

    private final SettingConfiguration setting;
    private final List<String> list = new ArrayList<>();
    private TranslationConfiguration message;
    private TranslationConfiguration command;
    private JsonConfig json;
    private MariaDBConfig mariadb;
    private MongoDBConfig mongodb;
    private MySQLConfig mysql;
    private PostgreSQLConfig postgresql;
    private SQLiteConfig sqlite;
    private boolean useStorage;
    private boolean isUpdated;

    public Configurations(RSPlugin plugin) {
        this.plugin = plugin;
        setting = new SettingConfiguration(plugin);
        message = new TranslationConfiguration(plugin, TranslationType.MESSAGE, setting.getLocale());
        command = new TranslationConfiguration(plugin, TranslationType.COMMAND, setting.getLocale());
    }

    public void initStorage(String... list) {
        this.list.clear();
        this.list.addAll(List.of(list));
        isUpdated = true;
        storage();
    }

    public void initStorage(List<String> list) {
        this.list.clear();
        this.list.addAll(list);
        isUpdated = true;
        storage();
    }

    private void storage() {
        this.useStorage = true;
        if (!list.isEmpty()) {
            json = new JsonConfig(plugin);
            mariadb = new MariaDBConfig(plugin);
            mongodb = new MongoDBConfig(plugin);
            mysql = new MySQLConfig(plugin);
            postgresql = new PostgreSQLConfig(plugin);
            sqlite = new SQLiteConfig(plugin);
        }
        loadStorage();
    }

    public void reload() {
        final String locale = setting.getLocale();
        setting.reload();
        if (locale.equalsIgnoreCase(setting.getLocale())) {
            message.reload();
            command.reload();
        } else {
            message = new TranslationConfiguration(plugin, TranslationType.MESSAGE, setting.getLocale());
            command = new TranslationConfiguration(plugin, TranslationType.COMMAND, setting.getLocale());
        }

        if (json != null) json.reload();
        if (mariadb != null) mariadb.reload();
        if (mongodb != null) mongodb.reload();
        if (mysql != null) mysql.reload();
        if (postgresql != null) postgresql.reload();
        if (sqlite != null) sqlite.reload();
        if (useStorage) loadStorage();
    }

    private void loadStorage() {
        StorageType type = setting.getStorage();
        Storage storage = plugin.getStorage();
        switch (type) {
            case JSON -> {
                if (!(storage instanceof Json) || json.isChanged() || isUpdated) {
                    for (String name : list) FileResource.createFile(plugin.getDataFolder() + "/Data", name + ".json");
                    File[] files = FileResource.createFolder(plugin.getDataFolder() + "/Data").listFiles();
                    assert files != null;
                    if (storage != null) storage.close();
                    plugin.setStorage(new Json(plugin, files));
                    plugin.console("Storage: Json");
                }
            }
            case MARIADB -> {
                if (!(storage instanceof MariaDB) || mariadb.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MariaDB(plugin, list));
                    plugin.console("Storage: MariaDB");
                }
            }
            case MONGODB -> {
                if (!(storage instanceof MongoDB) || mongodb.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MongoDB(plugin));
                    plugin.console("Storage: MongoDB");
                }
            }
            case MYSQL -> {
                if (!(storage instanceof MySQL) || mysql.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MySQL(plugin, list));
                    plugin.console("Storage: MySQL");
                }
            }
        }
        isUpdated = false;
    }
}
