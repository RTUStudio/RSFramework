package kr.rtustudio.framework.bukkit.api.core.configuration;

import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * 프레임워크 공통 번역 리소스를 조회하는 인터페이스입니다.
 *
 * <p>메시지 및 명령어 번역 타입별로 키 기반 조회를 지원합니다.
 */
public interface CommonTranslation {

    /**
     * 기본 로케일로 번역 문자열을 조회한다.
     *
     * @param type 번역 타입 (MESSAGE 또는 COMMAND)
     * @param key 번역 키
     * @return 번역된 문자열, 없으면 빈 문자열
     */
    @NotNull
    String get(TranslationType type, String key);

    /**
     * 지정한 로케일로 번역 문자열을 조회한다.
     *
     * @param type 번역 타입
     * @param locale 로케일 코드 (예: {@code "ko_kr"})
     * @param key 번역 키
     * @return 번역된 문자열
     */
    @NotNull
    String get(TranslationType type, String locale, String key);

    /**
     * 기본 로케일로 번역 문자열 리스트를 조회한다.
     *
     * @param type 번역 타입
     * @param key 번역 키
     * @return 번역된 문자열 리스트
     */
    @NotNull
    List<String> getList(TranslationType type, String key);

    /**
     * 지정한 로케일로 번역 문자열 리스트를 조회한다.
     *
     * @param type 번역 타입
     * @param locale 로케일 코드
     * @param key 번역 키
     * @return 번역된 문자열 리스트
     */
    @NotNull
    List<String> getList(TranslationType type, String locale, String key);
}
