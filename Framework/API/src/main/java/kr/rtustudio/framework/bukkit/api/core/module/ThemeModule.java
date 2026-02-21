package kr.rtustudio.framework.bukkit.api.core.module;

/** 플러그인 접두사 및 시스템 메시지의 테마(그라데이션 색상, 접두/접미사)를 정의하는 모듈입니다. */
public interface ThemeModule extends Module {

    /** 그라데이션 시작 색상 코드를 반환한다. */
    String getGradientStart();

    /** 그라데이션 끝 색상 코드를 반환한다. */
    String getGradientEnd();

    /** 플러그인 이름 앞에 붙는 접두사를 반환한다. */
    String getPrefix();

    /** 플러그인 이름 뒤에 붙는 접미사를 반환한다. */
    String getSuffix();

    /** 시스템 메시지 호버 텍스트를 반환한다. */
    String getSystemMessage();
}
