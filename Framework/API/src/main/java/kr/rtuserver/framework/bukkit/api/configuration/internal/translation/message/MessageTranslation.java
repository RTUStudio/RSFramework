package kr.rtuserver.framework.bukkit.api.configuration.internal.translation.message;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationType;

@SuppressWarnings("unused")
public class MessageTranslation extends TranslationConfiguration {

    public static String RELOAD = "reload";
    public static String NO_PERMISSION = "noPermission";
    public static String WRONG_USAGE = "wrongUsage";
    public static String ONLY_PLAYER = "onlyPlayer";
    public static String ONLY_CONSOLE = "onlyConsole";
    public static String NOT_FOUND_ONLINE_PLAYER = "notFound.onlinePlayer";
    public static String NOT_FOUND_OFFLINE_PLAYER = "notFound.offlinePlayer";
    public static String ERROR_INVENTORY = "error.inventory";
    public static String ERROR_COOLDOWN = "error.cooldown";

    public MessageTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }

}