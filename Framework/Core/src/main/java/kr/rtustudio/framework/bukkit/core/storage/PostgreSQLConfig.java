package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.storage.postgresql.PostgreSQL;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class PostgreSQLConfig extends RSConfiguration.Wrapper<RSPlugin>
        implements PostgreSQL.Config {

    private String host = "127.0.0.1";
    private int port = 5432;
    private String database = "";
    private String username = "";
    private String password = "";
    private String tablePrefix = getPlugin().getName() + "_";

    public PostgreSQLConfig(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "PostgreSQL"));
        setup(this);
    }

    private void init() {
        host = getString("host", host);
        port = getInt("port", port);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("table-prefix", tablePrefix);
    }
}
