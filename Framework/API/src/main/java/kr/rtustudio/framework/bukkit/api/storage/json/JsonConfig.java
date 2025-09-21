package kr.rtustudio.framework.bukkit.api.storage.json;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class JsonConfig extends RSConfiguration.Wrapper<RSPlugin> {

    private int savePeriod = 10;

    public JsonConfig(RSPlugin plugin) {
        super(plugin, "Configs/Storages", "Json.yml");
        setup(this);
    }

    private void init() {
        savePeriod = getInt("save-period", savePeriod);
    }
}
