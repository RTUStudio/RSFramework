package kr.rtuserver.framework.bukkit.api.configuration.translation.message;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MessageTranslation extends TranslationConfiguration {

    public MessageTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Common {

        RELOAD("reload"),
        NO_PERMISSION("noPermission"),
        WRONG_USAGE("wrongUsage"),
        ONLY_PLAYER("onlyPlayer"),
        ONLY_CONSOLE("onlyConsole"),
        NOT_FOUND_ONLINE_PLAYER("notFound.onlinePlayer"),
        NOT_FOUND_OFFLINE_PLAYER("notFound.offlinePlayer"),
        ERROR_INVENTORY("error.inventory"),
        ERROR_COOLDOWN("error.cooldown");

        private final String key;

    }

}