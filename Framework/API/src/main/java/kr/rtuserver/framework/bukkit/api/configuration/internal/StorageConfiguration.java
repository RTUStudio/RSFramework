package kr.rtuserver.framework.bukkit.api.configuration.internal;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.type.StorageType;
import kr.rtuserver.framework.bukkit.api.platform.FileResource;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import kr.rtuserver.framework.bukkit.api.storage.json.Json;
import kr.rtuserver.framework.bukkit.api.storage.json.JsonConfig;
import kr.rtuserver.framework.bukkit.api.storage.mariadb.MariaDB;
import kr.rtuserver.framework.bukkit.api.storage.mariadb.MariaDBConfig;
import kr.rtuserver.framework.bukkit.api.storage.mongodb.MongoDB;
import kr.rtuserver.framework.bukkit.api.storage.mongodb.MongoDBConfig;
import kr.rtuserver.framework.bukkit.api.storage.mysql.MySQL;
import kr.rtuserver.framework.bukkit.api.storage.mysql.MySQLConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class StorageConfiguration {

    private final RSPlugin plugin;

    private final List<String> list = new ArrayList<>();

    @Getter private JsonConfig json;
    @Getter private MariaDBConfig mariadb;
    @Getter private MongoDBConfig mongodb;
    @Getter private MySQLConfig mysql;

    public void init(String... list) {
        this.init(List.of(list));
    }

    public void init(List<String> list) {
        if (list.isEmpty()) return;
        this.list.addAll(list);
        json = new JsonConfig(plugin);
        mariadb = new MariaDBConfig(plugin);
        mongodb = new MongoDBConfig(plugin);
        mysql = new MySQLConfig(plugin);
        load();
    }

    private void load() {
        StorageType type = plugin.getConfiguration().getSetting().getStorage();
        Storage storage = plugin.getStorage();
        switch (type) {
            case JSON -> {
                if (!(storage instanceof Json) || json.isChanged()) {
                    for (String name : list)
                        FileResource.createFile(plugin.getDataFolder() + "/Data", name + ".json");
                    File[] files =
                            FileResource.createFolder(plugin.getDataFolder() + "/Data").listFiles();
                    assert files != null;
                    if (storage != null) storage.close();
                    plugin.setStorage(new Json(plugin, files));
                    plugin.console("Storage: Json");
                }
            }
            case MARIADB -> {
                if (!(storage instanceof MariaDB) || mariadb.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MariaDB(plugin, list));
                    plugin.console("Storage: MariaDB");
                }
            }
            case MONGODB -> {
                if (!(storage instanceof MongoDB) || mongodb.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MongoDB(plugin));
                    plugin.console("Storage: MongoDB");
                }
            }
            case MYSQL -> {
                if (!(storage instanceof MySQL) || mysql.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MySQL(plugin, list));
                    plugin.console("Storage: MySQL");
                }
            }
        }
    }

    public void reload() {
        if (list.isEmpty()) return;
        if (json != null) json.reload();
        if (mariadb != null) mariadb.reload();
        if (mongodb != null) mongodb.reload();
        if (mysql != null) mysql.reload();
        load();
    }
}
