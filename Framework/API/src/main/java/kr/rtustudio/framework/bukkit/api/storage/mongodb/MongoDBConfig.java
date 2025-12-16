package kr.rtustudio.framework.bukkit.api.storage.mongodb;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class MongoDBConfig extends RSConfiguration.Wrapper<RSPlugin> {

    private String host = "127.0.0.1";
    private String port = "27017";
    private String database = "RSPlugin";
    private String username = "root";
    private String password = "pwd";
    private String collectionPrefix = getPlugin().getName() + "_";

    public MongoDBConfig(RSPlugin plugin) {
        super(plugin, "Configs/Storages", "MongoDB.yml", null);
        setup(this);
    }

    private void init() {
        host = getString("host", host);
        port = getString("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        collectionPrefix = getString("collection-prefix", collectionPrefix);
    }
}
