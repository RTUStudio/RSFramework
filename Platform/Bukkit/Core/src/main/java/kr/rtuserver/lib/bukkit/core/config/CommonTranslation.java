package kr.rtuserver.lib.bukkit.core.config;

import kr.rtuserver.lib.bukkit.api.RSPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommonTranslation implements kr.rtuserver.lib.bukkit.api.core.config.CommonTranslation {

    private final RSPlugin plugin;

    public String getCommand(String key) {
        return plugin.getConfigurations().getCommand().get("common." + key);
    }

    public String getMessage(String key) {
        return plugin.getConfigurations().getMessage().get("common." + key);
    }
}
