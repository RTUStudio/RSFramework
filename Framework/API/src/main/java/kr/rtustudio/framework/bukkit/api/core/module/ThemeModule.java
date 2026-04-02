package kr.rtustudio.framework.bukkit.api.core.module;

/**
 * Module that defines the theme properties (gradient colors, prefixes, suffixes) for plugin
 * prefixes and system messages.
 *
 * <p>플러그인 접두사 및 시스템 메시지의 테마(그라데이션 색상, 접두/접미사)를 정의하는 모듈.
 */
public interface ThemeModule extends Module {

    /**
     * Returns the gradient start color code.
     *
     * <p>그라데이션 시작 색상 코드를 반환한다.
     */
    String getGradientStart();

    /**
     * Returns the gradient end color code.
     *
     * <p>그라데이션 끝 색상 코드를 반환한다.
     */
    String getGradientEnd();

    /**
     * Returns the prefix attached before the plugin name.
     *
     * <p>플러그인 이름 앞에 붙는 접두사를 반환한다.
     */
    String getPrefix();

    /**
     * Returns the suffix attached after the plugin name.
     *
     * <p>플러그인 이름 뒤에 붙는 접미사를 반환한다.
     */
    String getSuffix();

    /**
     * Returns the system message hover text.
     *
     * <p>시스템 메시지 호버 텍스트를 반환한다.
     */
    String getSystemMessage();
}
