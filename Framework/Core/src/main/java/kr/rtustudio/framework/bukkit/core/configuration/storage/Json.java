package kr.rtustudio.framework.bukkit.core.configuration.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class Json extends RSConfiguration.Wrapper<RSPlugin>
        implements kr.rtustudio.storage.json.Json.Config {
    private int savePeriod = 10;
    private String dataFolder = "Data";
    private boolean changed = false;

    public Json(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "Json"));
        setup(this);
    }

    private void init() {
        int oldSavePeriod = savePeriod;
        String oldDataFolder = dataFolder;
        savePeriod = getInt("save-period", savePeriod);
        dataFolder = getString("data-folder", dataFolder);
        changed = oldSavePeriod != savePeriod || !dataFolder.equals(oldDataFolder);
    }

    public boolean isChanged() {
        boolean wasChanged = changed;
        changed = false;
        return wasChanged;
    }

    @Override
    public String getDataFolder() {
        if (dataFolder.startsWith("/")) return dataFolder;
        return getPlugin().getDataFolder().getPath() + "/" + dataFolder;
    }
}
