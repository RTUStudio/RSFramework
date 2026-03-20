package kr.rtustudio.bridge.proxium.api.proxy.request;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

import org.jspecify.annotations.Nullable;

/**
 * 크로스 서버 텔레포트 요청 패킷.
 *
 * <p>플레이어 대상 또는 위치 대상 텔레포트를 하나의 레코드로 통합한다.
 *
 * @param player 텔레포트할 플레이어
 * @param targetPlayer 대상 플레이어 (플레이어 TP 시)
 * @param targetLocation 대상 위치 (위치 TP 시)
 */
public record TeleportRequest(
        ProxyPlayer player,
        @Nullable ProxyPlayer targetPlayer,
        @Nullable ProxyLocation targetLocation) {

    /** 플레이어 대상 텔레포트 */
    public TeleportRequest(ProxyPlayer player, ProxyPlayer target) {
        this(player, target, null);
    }

    /** 위치 대상 텔레포트 */
    public TeleportRequest(ProxyPlayer player, ProxyLocation location) {
        this(player, null, location);
    }

    /** 라우팅용 대상 서버 이름 */
    public String server() {
        if (targetLocation != null) return targetLocation.server().name();
        if (targetPlayer != null) return targetPlayer.getServer();
        return null;
    }
}
