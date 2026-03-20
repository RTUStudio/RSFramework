package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 프록시 및 로컬 서버의 온라인 플레이어 목록을 조회하는 유틸리티 클래스입니다.
 *
 * <p>Proxium 연결 시 프록시 전체 플레이어를, 미연결 시 현재 서버의 로컬 플레이어만 반환합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerList {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * UUID로 플레이어를 조회한다.
     *
     * <p>Proxium 플레이어 맵에서 직접 조회한다.
     *
     * @param uniqueId 플레이어 UUID
     * @return 프록시 플레이어, 없으면 {@code null}
     */
    @Nullable
    public static ProxyPlayer getPlayer(UUID uniqueId) {
        return framework().getBridge(Proxium.class).getPlayers().get(uniqueId);
    }

    /**
     * 프록시 포함 전체 온라인 플레이어 목록을 반환한다.
     *
     * @return 플레이어 집합
     */
    @NotNull
    public static Set<ProxyPlayer> getPlayers() {
        return new HashSet<>(framework().getBridge(Proxium.class).getPlayers().values());
    }
}
