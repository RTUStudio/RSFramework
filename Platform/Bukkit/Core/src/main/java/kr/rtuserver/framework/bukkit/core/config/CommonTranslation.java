package kr.rtuserver.framework.bukkit.core.config;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommonTranslation implements kr.rtuserver.framework.bukkit.api.core.config.CommonTranslation {

    private final RSPlugin plugin;

    public String getCommand(String key) {
        return plugin.getConfigurations().getCommand().get("common." + key);
    }

    public String getMessage(String key) {
        return plugin.getConfigurations().getMessage().get("common." + key);
    }
}
