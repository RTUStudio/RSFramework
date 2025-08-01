package kr.rtuserver.framework.bukkit.core.configuration;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationType;
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
    public String get(TranslationType type, String locale, String key) {
        return switch (type) {
            case COMMAND -> getCommand(locale, key);
            case MESSAGE -> getMessage(locale, key);
        };
    }

    private String getCommand(String locale, String key) {
        return plugin.getConfigurations().getCommand().get(locale, "common." + key);
    }

    private String getMessage(String locale, String key) {
        return plugin.getConfigurations().getMessage().get(locale, "common." + key);
    }

}
