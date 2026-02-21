package kr.rtustudio.framework.bukkit.core.configuration.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class SQLite extends RSConfiguration.Wrapper<RSPlugin>
        implements kr.rtustudio.storage.sqlite.SQLite.Config {
    private String filePath = "Data/SQLite.db";
    private boolean changed = false;

    public SQLite(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "SQLite"));
        setup(this);
    }

    private void init() {
        String oldFilePath = filePath;
        filePath = getString("file-path", filePath);
        changed = !filePath.equals(oldFilePath);
    }

    public boolean isChanged() {
        boolean wasChanged = changed;
        changed = false;
        return wasChanged;
    }

    @Override
    public String getFilePath() {
        if (filePath.startsWith("/")) return filePath;
        return getPlugin().getDataFolder().getPath() + "/" + filePath;
    }
}
