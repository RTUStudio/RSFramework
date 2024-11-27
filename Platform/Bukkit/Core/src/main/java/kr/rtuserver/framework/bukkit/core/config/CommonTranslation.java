package kr.rtuserver.framework.bukkit.core.config;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class CommonTranslation implements kr.rtuserver.framework.bukkit.api.core.config.CommonTranslation {

    private final RSPlugin plugin;

    public String getCommand(String key) {
        return getCommand(null, key);
    }

    public String getCommand(CommandSender sender, String key) {
        return plugin.getConfigurations().getCommand().get(sender, "common." + key);
    }

    public String getMessage(String key) {
        return getMessage(null, key);
    }

    public String getMessage(CommandSender sender, String key) {
        return plugin.getConfigurations().getMessage().get(sender, "common." + key);
    }
}
