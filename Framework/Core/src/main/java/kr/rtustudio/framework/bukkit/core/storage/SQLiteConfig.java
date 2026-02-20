package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.storage.sqlite.SQLite;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class SQLiteConfig extends RSConfiguration.Wrapper<RSPlugin> implements SQLite.Config {

    private String filePath = "Data/SQLite.db";

    public SQLiteConfig(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "SQLite"));
        setup(this);
    }

    private void init() {
        filePath = getString("file-path", filePath);
    }

    @Override
    public String getFilePath() {
        if (filePath.startsWith("/")) return filePath;
        return getPlugin().getDataFolder().getPath() + "/" + filePath;
    }
}
