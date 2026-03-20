package kr.rtustudio.bridge.proxium.api.handler;

import kr.rtustudio.bridge.proxium.api.netty.Connection;

/** 커스텀 프로토콜 단위의 패킷 처리를 담당하는 네트워크 연결(Connection) 라이프사이클 핸들러. */
public interface ConnectionHandler {
    /**
     * 연결이 준비되어 패킷을 양방향으로 송수신할 수 있는 상태가 되었을 때 처음 한 번만 호출된다.
     *
     * @param connection 현재 성립된 연결 객체.
     */
    default void onReady(Connection connection) {}

    /**
     * 파이프라인 오류, 수동 연결 끊기 등에 의해 네트워크 연결이 완전히 끊긴 직후에 호출된다.
     *
     * @param connection 종료된 연결 객체.
     */
    default void onDisconnect(Connection connection) {}

    /**
     * 프로토콜을 통해 수신된 모든 디코딩 완료 패킷에 대해 매번 호출된다.
     *
     * @param connection 현재 패킷을 받은 연결 객체.
     * @param packet 디코딩된 패킷 객체. "instanceof" 문법을 사용하여 구체적인 패킷 유형을 검증해야 한다.
     */
    default void handlePacket(Connection connection, Object packet) {}
}
