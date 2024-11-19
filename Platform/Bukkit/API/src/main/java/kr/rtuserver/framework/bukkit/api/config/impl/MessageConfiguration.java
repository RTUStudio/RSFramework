package kr.rtuserver.framework.bukkit.api.config.impl;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.RSConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.core.modules.ThemeModule;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MessageConfiguration extends RSConfiguration {

    private final Map<String, Object> map = new HashMap<>();
    private final Framework framework = LightDI.getBean(Framework.class);

    public MessageConfiguration(RSPlugin plugin, String file) {
        super(plugin, "Translations/Message", "Locale_" + file + ".yml", null);
        setup(this);
    }

    private void init() {
        getString("prefix", "");
        for (String key : getConfig().getKeys(true)) {
            if (getConfig().isString(key)) {
                map.put(key, getString(key, ""));
            }
        }
    }

    public Component getPrefix() {
        String name = get("prefix");
        if (name.isEmpty()) name = getPlugin().getName();
        ThemeModule theme = framework.getModules().getThemeModule();
        String text = String.format("<gradient:%s:%s>%s%s%s</gradient>",
                theme.getGradientStart(),
                theme.getGradientEnd(),
                theme.getPrefix(),
                name,
                theme.getSuffix());
        return ComponentFormatter.mini(text);
    }

    public String get(String key) {
        return map.getOrDefault(key, "").toString();
    }

    public String getCommon(String key) {
        return framework.getCommonTranslation().getMessage(key);
    }
}