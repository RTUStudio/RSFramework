package kr.rtuserver.framework.bukkit.api.configuration.translation.command;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class CommandTranslation extends TranslationConfiguration {

    public CommandTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }

    public static String RELOAD_NAME = "reload.name";
    public static String RELOAD_DESCRIPTION = "reload.description";

}