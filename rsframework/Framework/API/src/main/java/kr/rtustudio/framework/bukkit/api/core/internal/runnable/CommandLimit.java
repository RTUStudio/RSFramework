package kr.rtustudio.framework.bukkit.api.core.internal.runnable;

import java.util.Map;
import java.util.UUID;

/**
 * 명령어 실행 쿨다운을 관리하는 인터페이스입니다.
 *
 * <p>{@link Runnable}을 구현하여 주기적으로 쿨다운 틱을 차감합니다.
 */
public interface CommandLimit extends Runnable {

    /** 플레이어별 남은 쿨다운 틱 수 맵을 반환한다. */
    Map<UUID, Integer> getExecuteLimit();
}
