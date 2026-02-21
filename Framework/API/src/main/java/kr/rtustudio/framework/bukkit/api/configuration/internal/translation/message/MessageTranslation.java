package kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;

/**
 * 메시지 번역을 관리하는 클래스입니다.
 *
 * <p>{@code Translations/Message/<locale>.yml} 파일을 로드하며, 안내/오류 메시지의 다국어 지원을 제공합니다.
 */
@SuppressWarnings("unused")
public class MessageTranslation extends TranslationConfiguration {

    public static String RELOAD = "reload";
    public static String NO_PERMISSION = "no-permission";
    public static String WRONG_USAGE = "wrong-usage";
    public static String ONLY_PLAYER = "only-player";
    public static String ONLY_CONSOLE = "only-console";
    public static String NOT_FOUND_ONLINE_PLAYER = "not-found.online-player";
    public static String NOT_FOUND_OFFLINE_PLAYER = "not-found.offline-player";
    public static String NOT_FOUND_ITEM = "not-found.item";
    public static String ERROR_INVENTORY = "error.inventory";
    public static String ERROR_COOLDOWN = "error.cooldown";

    public MessageTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }
}
