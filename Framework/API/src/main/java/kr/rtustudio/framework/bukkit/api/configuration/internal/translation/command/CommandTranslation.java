package kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;

@SuppressWarnings("unused")
public class CommandTranslation extends TranslationConfiguration {

    public static String RELOAD_NAME = "reload.name";
    public static String RELOAD_DESCRIPTION = "reload.description";

    public CommandTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }
}
