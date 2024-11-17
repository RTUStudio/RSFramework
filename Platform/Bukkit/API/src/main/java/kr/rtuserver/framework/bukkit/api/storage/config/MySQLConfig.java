package kr.rtuserver.framework.bukkit.api.storage.config;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.RSConfiguration;
import lombok.Getter;

@Getter
public class MySQLConfig extends RSConfiguration {
    private String host = "127.0.0.1";
    private String port = "3306";
    private String database = "";
    private String username = "";
    private String password = "";
    private String tablePrefix = getPlugin().getName() + "_";
    private boolean useArrowOperator = true;

    public MySQLConfig(RSPlugin plugin) {
        super(plugin, "Configs/Storages", "MySQL.yml", null);
        setup(this);
    }

    private void init() {
        host = getString("host", host);
        port = getString("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("tablePrefix", tablePrefix);
        useArrowOperator = getBoolean("UseArrowOperator", useArrowOperator);
    }

}

