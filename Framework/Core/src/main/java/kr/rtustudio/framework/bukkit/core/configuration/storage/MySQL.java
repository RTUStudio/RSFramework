package kr.rtustudio.framework.bukkit.core.configuration.storage;

import kr.rtustudio.configure.ConfigPath;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class MySQL extends RSConfiguration.Wrapper<RSPlugin>
        implements kr.rtustudio.storage.mysql.MySQL.Config {
    private String host = "127.0.0.1";
    private int port = 3306;
    private String database = "RSPlugin";
    private String username = "root";
    private String password = "pwd";
    private String tablePrefix = getPlugin().getName() + "_";
    private boolean useArrowOperator = true;
    private boolean changed = false;

    public MySQL(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "MySQL"));
        setup(this);
    }

    private void init() {
        String oldHost = host;
        int oldPort = port;
        String oldDatabase = database;
        String oldUsername = username;
        String oldPassword = password;
        String oldTablePrefix = tablePrefix;
        boolean oldUseArrowOperator = useArrowOperator;
        host = getString("host", host);
        port = getInt("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("table-prefix", tablePrefix);
        useArrowOperator = getBoolean("use-arrow-operator", useArrowOperator);
        changed =
                !host.equals(oldHost)
                        || port != oldPort
                        || !database.equals(oldDatabase)
                        || !username.equals(oldUsername)
                        || !password.equals(oldPassword)
                        || !tablePrefix.equals(oldTablePrefix)
                        || useArrowOperator != oldUseArrowOperator;
    }

    public boolean isChanged() {
        boolean wasChanged = changed;
        changed = false;
        return wasChanged;
    }
}
