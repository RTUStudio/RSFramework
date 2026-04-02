package kr.rtustudio.framework.bukkit.api.core.provider.name;

import kr.rtustudio.framework.bukkit.api.core.provider.Provider;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider that provides player name and UUID lookup functionalities.
 *
 * <p>플레이어 이름과 UUID 조회 기능을 제공하는 프로바이더. 로컬 서버 또는 전체 네트워크 범위의 플레이어 목록을 조회할 수 있다.
 */
public interface NameProvider extends Provider {

    /**
     * Returns a list of player names in the specified scope.
     *
     * <p>지정한 범위의 플레이어 이름 목록을 반환한다.
     *
     * @param scope lookup scope ({@link Scope#LOCAL} or {@link Scope#GLOBAL})
     * @return list of player names
     */
    @NotNull
    List<String> names(Scope scope);

    /**
     * Returns a list of all player names in the current server.
     *
     * <p>현재 서버의 모든 플레이어 이름을 반환한다.
     *
     * @return list of player names
     */
    default @NotNull List<String> names() {
        return names(Scope.LOCAL);
    }

    /**
     * Looks up a player's name by UUID.
     *
     * <p>UUID로 플레이어 이름을 조회한다.
     *
     * @param uniqueId player UUID
     * @return player name, or {@code null} if not found
     */
    @Nullable
    String getName(UUID uniqueId);

    /**
     * Looks up a player's UUID by name.
     *
     * <p>이름으로 플레이어 UUID를 조회한다.
     *
     * @param name player name
     * @return player UUID, or {@code null} if not found
     */
    @Nullable
    UUID getUniqueId(String name);

    /** Player lookup scope. / 플레이어 조회 범위. */
    enum Scope {
        /** All servers. / 모든 서버 */
        GLOBAL,
        /** Current server. / 현재 서버 */
        LOCAL
    }
}
