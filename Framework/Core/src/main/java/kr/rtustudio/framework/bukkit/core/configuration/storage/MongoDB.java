package kr.rtustudio.framework.bukkit.core.configuration.storage;

import kr.rtustudio.configure.ConfigPath;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class MongoDB extends RSConfiguration.Wrapper<RSPlugin>
        implements kr.rtustudio.storage.mongodb.MongoDB.Config {
    private String host = "127.0.0.1";
    private int port = 27017;
    private String database = "RSPlugin";
    private String username = "root";
    private String password = "pwd";
    private String collectionPrefix = getPlugin().getName() + "_";
    private boolean changed = false;

    public MongoDB(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "MongoDB"));
        setup(this);
    }

    private void init() {
        String oldHost = host;
        int oldPort = port;
        String oldDatabase = database;
        String oldUsername = username;
        String oldPassword = password;
        String oldCollectionPrefix = collectionPrefix;
        host = getString("host", host);
        port = getInt("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        collectionPrefix = getString("collection-prefix", collectionPrefix);
        changed =
                !host.equals(oldHost)
                        || port != oldPort
                        || !database.equals(oldDatabase)
                        || !username.equals(oldUsername)
                        || !password.equals(oldPassword)
                        || !collectionPrefix.equals(oldCollectionPrefix);
    }

    public boolean isChanged() {
        boolean wasChanged = changed;
        changed = false;
        return wasChanged;
    }
}
