package kr.rtuserver.framework.bukkit.api.storage.wip;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class SQLiteConfig extends RSConfiguration.Wrapper<RSPlugin> {

    private String file = "./Data/SQLite.sql";
    private String database = "";
    private String username = "";
    private String password = "";
    private String tablePrefix = getPlugin().getName() + "_";

    public SQLiteConfig(RSPlugin plugin) {
        super(plugin, "Configs/Storages", "SQLite.yml", null);
        setup(this);
    }

    private void init() {
        file = getString("file", file);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("tablePrefix", tablePrefix);
    }
}
