package kr.rtustudio.framework.bukkit.api.core.provider.name;

import kr.rtustudio.framework.bukkit.api.core.provider.Provider;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 플레이어 이름과 UUID 조회 기능을 제공하는 프로바이더입니다.
 *
 * <p>로컬 서버 또는 전체 네트워크 범위의 플레이어 목록을 조회할 수 있습니다.
 */
public interface NameProvider extends Provider {

    /**
     * 지정한 범위의 플레이어 이름 목록을 반환한다.
     *
     * @param scope 조회 범위 ({@link Scope#LOCAL} 또는 {@link Scope#GLOBAL})
     * @return 플레이어 이름 목록
     */
    @NotNull
    List<String> names(Scope scope);

    /**
     * 현재 서버의 모든 플레이어 이름을 반환한다.
     *
     * @return 플레이어 이름 목록
     */
    default @NotNull List<String> names() {
        return names(Scope.LOCAL);
    }

    /**
     * UUID로 플레이어 이름을 조회한다.
     *
     * @param uniqueId 플레이어 UUID
     * @return 플레이어 이름, 없으면 {@code null}
     */
    @Nullable
    String getName(UUID uniqueId);

    /**
     * 이름으로 플레이어 UUID를 조회한다.
     *
     * @param name 플레이어 이름
     * @return 플레이어 UUID, 없으면 {@code null}
     */
    @Nullable
    UUID getUniqueId(String name);

    /** 플레이어 조회 범위. */
    enum Scope {
        /** 모든 서버 */
        GLOBAL,
        /** 현재 서버 */
        LOCAL
    }
}
