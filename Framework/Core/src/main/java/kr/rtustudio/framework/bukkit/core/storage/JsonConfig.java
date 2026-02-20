package kr.rtustudio.framework.bukkit.core.storage;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.storage.json.Json;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class JsonConfig extends RSConfiguration.Wrapper<RSPlugin> implements Json.Config {

    private int savePeriod = 10;
    private String dataFolder = "Data";

    public JsonConfig(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage", "Json"));
        setup(this);
    }

    private void init() {
        savePeriod = getInt("save-period", savePeriod);
        dataFolder = getString("data-folder", dataFolder);
    }

    @Override
    public String getDataFolder() {
        if (dataFolder.startsWith("/")) return dataFolder;
        return getPlugin().getDataFolder().getPath() + "/" + dataFolder;
    }
}
