package kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;

/**
 * 명령어 번역을 관리하는 클래스입니다.
 *
 * <p>{@code Translations/Command/<locale>.yml} 파일을 로드하며, 명령어 이름 및 설명의 다국어 지원을 제공합니다.
 */
@SuppressWarnings("unused")
public class CommandTranslation extends TranslationConfiguration {

    public static String RELOAD_NAME = "reload.name";
    public static String RELOAD_DESCRIPTION = "reload.description";

    public CommandTranslation(RSPlugin plugin, TranslationType type, String defaultLocale) {
        super(plugin, type, defaultLocale);
    }
}
