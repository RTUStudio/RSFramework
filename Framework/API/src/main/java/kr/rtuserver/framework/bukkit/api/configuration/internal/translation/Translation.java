package kr.rtuserver.framework.bukkit.api.configuration.internal.translation;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@SuppressWarnings("unused")
public class Translation extends RSConfiguration.Wrapper<RSPlugin> {

    private final Map<String, String> map = new HashMap<>();

    public Translation(RSPlugin plugin, String folder, String lang) {
        super(plugin, "Translations/" + folder, lang + ".yml", null);
        setup(this);
    }

    private void init() {
        for (String key : keys()) {
            String result = getString(key, "");
            if (result.isEmpty()) continue;
            map.put(key, result);
        }
    }

    public String get(String key) {
        return map.getOrDefault(key, "");
    }

}
