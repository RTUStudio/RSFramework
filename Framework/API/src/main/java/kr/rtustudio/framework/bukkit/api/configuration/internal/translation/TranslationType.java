package kr.rtustudio.framework.bukkit.api.configuration.internal.translation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 번역 파일의 종류를 나타내는 열거형입니다. */
@Getter
@RequiredArgsConstructor
public enum TranslationType {
    /** 명령어 번역 (이름, 설명 등) */
    COMMAND("Command"),
    /** 메시지 번역 (안내, 오류 메시지 등) */
    MESSAGE("Message");

    private final String name;
}
