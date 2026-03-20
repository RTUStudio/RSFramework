package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

/**
 * 플레이어 상태 변경 이벤트 패킷.
 *
 * <p>프록시에서 전체 서버로 브로드캐스트되며, 서버 측에서 로컬 플레이어 맵을 업데이트한다.
 *
 * @param action 이벤트 유형
 * @param player 대상 플레이어 (최신 데이터 포함)
 */
public record PlayerEvent(Action action, ProxyPlayer player) {

    /** 플레이어 이벤트 유형. */
    public enum Action {
        /** 프록시 네트워크에 최초 접속 */
        JOIN,
        /** 프록시 네트워크에서 퇴장 */
        LEAVE,
        /** 다른 서버로 이동 (player.server()가 새 서버) */
        SWITCH
    }
}
