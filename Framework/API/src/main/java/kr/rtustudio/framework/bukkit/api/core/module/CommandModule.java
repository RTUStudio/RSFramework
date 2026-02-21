package kr.rtustudio.framework.bukkit.api.core.module;

/** 명령어 실행 제한(쿨다운) 설정을 제공하는 모듈입니다. */
public interface CommandModule extends Module {

    /** 명령어 실행 쿨다운 틱 수를 반환한다. 0 이하면 쿨다운 비활성화. */
    int getExecuteLimit();
}
