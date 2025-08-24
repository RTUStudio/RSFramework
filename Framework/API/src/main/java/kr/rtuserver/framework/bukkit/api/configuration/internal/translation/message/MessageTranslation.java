package kr.rtuserver.framework.bukkit.api.configuration.internal.translation.message;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationType;

@SuppressWarnings("unused")
public class MessageTranslation extends TranslationConfiguration {

    public static String RELOAD = "reload";
    public static String NO_PERMISSION = "no-permission";
    public static String WRONG_USAGE = "wrong-usage";
    public static String ONLY_PLAYER = "only-player";
    public static String ONLY_CONSOLE = "only-console";
    public static String NOT_FOUND_ONLINE_PLAYER = "not-found.online-player";
    public static String NOT_FOUND_OFFLINE_PLAYER = "not-found.offline-player";
    public static String ERROR_INVENTORY = "error.inventory";
    public static String ERROR_COOLDOWN = "error.cooldown";

    public MessageTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }

}