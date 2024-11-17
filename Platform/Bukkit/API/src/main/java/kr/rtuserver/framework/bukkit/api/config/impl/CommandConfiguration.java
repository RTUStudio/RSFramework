package kr.rtuserver.framework.bukkit.api.config.impl;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.RSConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommandConfiguration extends RSConfiguration {

    private final Map<String, Object> map = new HashMap<>();
    private final Framework framework = LightDI.getBean(Framework.class);

    public CommandConfiguration(RSPlugin plugin, String file) {
        super(plugin, "Translations/Command", "Locale_" + file + ".yml", null);
        setup(this);
    }

    private void init() {
        for (String key : getConfig().getKeys(true)) {
            if (getConfig().isString(key)) {
                String result = getString(key, "");
                if (result.isEmpty()) continue;
                map.put(key, getString(key, "error"));
            }
        }
    }

    public String get(String key) {
        return map.getOrDefault(key, "null").toString();
    }

    public String getCommon(String key) {
        return framework.getCommonTranslation().getCommand(key);
    }
}
