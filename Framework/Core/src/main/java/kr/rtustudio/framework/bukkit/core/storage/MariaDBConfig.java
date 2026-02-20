package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.storage.mariadb.MariaDB;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class MariaDBConfig extends RSConfiguration.Wrapper<RSPlugin> implements MariaDB.Config {

    private String host = "127.0.0.1";
    private String port = "3306";
    private String database = "RSPlugin";
    private String username = "root";
    private String password = "pwd";
    private String tablePrefix = getPlugin().getName() + "_";
    private boolean useArrowOperator = true;

    public MariaDBConfig(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "MariaDB"));
        setup(this);
    }

    private void init() {
        host = getString("host", host);
        port = getString("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("table-prefix", tablePrefix);
        useArrowOperator = getBoolean("use-arrow-operator", useArrowOperator);
    }
}
