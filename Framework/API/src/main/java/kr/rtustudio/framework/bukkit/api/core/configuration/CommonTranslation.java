package kr.rtustudio.framework.bukkit.api.core.configuration;

import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for querying framework-wide common translation resources.
 *
 * <p>프레임워크 공통 번역 리소스를 조회하는 인터페이스. 메시지 및 명령어 번역 타입별로 키 기반 조회를 지원한다.
 */
public interface CommonTranslation {

    /**
     * Retrieves a translation string in the default locale.
     *
     * <p>기본 로케일로 번역 문자열을 조회한다.
     *
     * @param type translation type (MESSAGE or COMMAND)
     * @param key translation key
     * @return translated string, or empty string if not found
     */
    @NotNull
    String get(TranslationType type, String key);

    /**
     * Retrieves a translation string in the specified locale.
     *
     * <p>지정한 로케일로 번역 문자열을 조회한다.
     *
     * @param type translation type
     * @param locale locale code (e.g. {@code "ko_kr"})
     * @param key translation key
     * @return translated string
     */
    @NotNull
    String get(TranslationType type, String locale, String key);

    /**
     * Retrieves a list of translation strings in the default locale.
     *
     * <p>기본 로케일로 번역 문자열 리스트를 조회한다.
     *
     * @param type translation type
     * @param key translation key
     * @return translated string list
     */
    @NotNull
    List<String> getList(TranslationType type, String key);

    /**
     * Retrieves a list of translation strings in the specified locale.
     *
     * <p>지정한 로케일로 번역 문자열 리스트를 조회한다.
     *
     * @param type translation type
     * @param locale locale code
     * @param key translation key
     * @return translated string list
     */
    @NotNull
    List<String> getList(TranslationType type, String locale, String key);
}
