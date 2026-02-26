package kr.rtustudio.framework.bukkit.core.configuration.storage;

import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class PostgreSQL extends RSConfiguration.Wrapper<RSPlugin>
        implements kr.rtustudio.storage.postgresql.PostgreSQL.Config {
    private String host = "127.0.0.1";
    private int port = 5432;
    private String database = "RSPlugin";
    private String username = "root";
    private String password = "pwd";
    private String tablePrefix = getPlugin().getName() + "_";
    private boolean changed = false;

    public PostgreSQL(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "PostgreSQL"));
        setup(this);
    }

    private void init() {
        String oldHost = host;
        int oldPort = port;
        String oldDatabase = database;
        String oldUsername = username;
        String oldPassword = password;
        String oldTablePrefix = tablePrefix;
        host = getString("host", host);
        port = getInt("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("table-prefix", tablePrefix);
        changed =
                !host.equals(oldHost)
                        || port != oldPort
                        || !database.equals(oldDatabase)
                        || !username.equals(oldUsername)
                        || !password.equals(oldPassword)
                        || !tablePrefix.equals(oldTablePrefix);
    }

    public boolean isChanged() {
        boolean wasChanged = changed;
        changed = false;
        return wasChanged;
    }
}
