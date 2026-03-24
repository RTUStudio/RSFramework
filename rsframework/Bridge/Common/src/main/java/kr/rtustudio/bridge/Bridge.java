package kr.rtustudio.bridge;

/**
 * Base bridge interface representing a network communication endpoint. Provides connection status
 * and lifecycle management only. Messaging capabilities are defined by sub-interfaces: {@link
 * Broadcast} for pub/sub, and {@code Transaction} for RPC.
 *
 * <p>네트워크 통신 엔드포인트를 나타내는 기본 브릿지 인터페이스. 연결 상태와 생명주기 관리만 제공한다. 메시징 기능은 하위 인터페이스에서 정의: Pub/Sub은 {@link
 * Broadcast}, RPC는 {@code Transaction}.
 */
public interface Bridge {

    /**
     * Checks whether this bridge is connected and active.
     *
     * <p>브릿지가 네트워크에 연결되어 활성화된 상태인지 확인한다.
     *
     * @return connection status
     */
    boolean isConnected();

    /**
     * Closes and cleans up all connections and channel communication for this bridge.
     *
     * <p>이 브릿지의 모든 연결과 채널 통신을 닫고 정리한다.
     */
    void close();
}
