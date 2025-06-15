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

    @Getter
    @RequiredArgsConstructor
    public enum Common {

        RELOAD_NAME("reload.name"),
        RELOAD_DESCRIPTION("reload.description");

        private final String key;

    }

}