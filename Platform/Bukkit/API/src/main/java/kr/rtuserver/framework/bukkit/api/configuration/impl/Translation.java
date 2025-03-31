package kr.rtuserver.framework.bukkit.api.configuration.impl;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;

import java.util.HashMap;
import java.util.Map;

public class Translation extends RSConfiguration<RSPlugin> {

    private final Map<String, String> map = new HashMap<>();

    public Translation(RSPlugin plugin, String folder, String lang) {
        super(plugin, "Translations/" + folder, lang + ".yml", null);
        setup(this);
    }

    private void init() {
        for (String key : getConfig().getKeys(true)) {
            String result = getString(key, "");
            if (result.isEmpty()) continue;
            map.put(key, result);
        }
    }

    public String get(String key) {
        return map.getOrDefault(key, "");
    }
}
