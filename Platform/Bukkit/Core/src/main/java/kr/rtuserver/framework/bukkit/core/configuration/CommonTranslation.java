package kr.rtuserver.framework.bukkit.core.configuration;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.impl.TranslationType;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class CommonTranslation implements kr.rtuserver.framework.bukkit.api.core.configuration.CommonTranslation {

    private final RSPlugin plugin;

    @Override
    public String get(TranslationType type, String key) {
        return get(type, null, key);
    }

    @Override
    public String get(TranslationType type, CommandSender sender, String key) {
        return switch (type) {
            case COMMAND -> getCommand(sender, key);
            case MESSAGE -> getMessage(sender, key);
        };
    }

    private String getCommand(CommandSender sender, String key) {
        return plugin.getConfigurations().getCommand().get(sender, "common." + key);
    }

    private String getMessage(CommandSender sender, String key) {
        return plugin.getConfigurations().getMessage().get(sender, "common." + key);
    }
}
