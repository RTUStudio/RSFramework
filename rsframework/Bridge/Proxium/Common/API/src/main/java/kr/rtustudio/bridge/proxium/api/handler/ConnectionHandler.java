package kr.rtustudio.bridge.proxium.api.handler;

import kr.rtustudio.bridge.proxium.api.netty.Connection;

/**
 * Connection lifecycle handler responsible for per-protocol packet processing.
 *
 * <p>커스텀 프로토콜 단위의 패킷 처리를 담당하는 네트워크 연결 라이프사이클 핸들러.
 */
public interface ConnectionHandler {
    /**
     * Called once when the connection is ready for bidirectional packet exchange.
     *
     * <p>연결이 준비되어 패킷을 양방향으로 송수신할 수 있는 상태가 되었을 때 처음 한 번만 호출된다.
     *
     * @param connection the established connection
     */
    default void onReady(Connection connection) {}

    /**
     * Called after the network connection is completely severed (pipeline error, manual disconnect,
     * etc.).
     *
     * <p>파이프라인 오류, 수동 연결 끊기 등에 의해 네트워크 연결이 완전히 끊긴 직후에 호출된다.
     *
     * @param connection the terminated connection
     */
    default void onDisconnect(Connection connection) {}

    /**
     * Called for every decoded packet received through the protocol.
     *
     * <p>프로토콜을 통해 수신된 모든 디코딩 완료 패킷에 대해 매번 호출된다.
     *
     * @param connection the connection that received the packet
     * @param packet decoded packet object; use {@code instanceof} to check the specific type
     */
    default void handlePacket(Connection connection, Object packet) {}
}
