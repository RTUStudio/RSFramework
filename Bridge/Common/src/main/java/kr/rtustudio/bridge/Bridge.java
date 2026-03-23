package kr.rtustudio.bridge;

import java.util.function.Consumer;

/**
 * Common bridge interface used across various network layers such as Proxium and Redis.
 *
 * <p>프록시움과 레디스 등 다양한 네트워크 계층에서 사용되는 공통 브릿지 인터페이스.
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
     * Binds and registers packet classes for a specific channel.
     *
     * <p>특정 채널에 사용할 패킷 클래스들을 바인딩하여 등록한다.
     *
     * @param channel channel for sending/receiving messages
     * @param types varargs of packet classes to register
     */
    void register(BridgeChannel channel, Class<?>... types);

    /**
     * Registers a per-type subscription handler on a specific channel. Re-registering the same type
     * replaces the existing handler.
     *
     * <p>특정 채널에 메시지 타입별 구독 핸들러를 등록한다. 동일 타입에 대해 다시 호출하면 기존 핸들러가 교체된다.
     *
     * @param channel channel to subscribe
     * @param type message type class
     * @param handler callback for received messages
     * @param <T> message type
     */
    <T> void subscribe(BridgeChannel channel, Class<T> type, Consumer<T> handler);

    /**
     * Publishes (broadcasts) a message to all nodes subscribing to the channel.
     *
     * <p>특정 채널을 구독하고 있는 모든 노드에게 메시지를 발행(브로드캐스트)한다.
     *
     * @param channel channel to publish on
     * @param message packet object to send
     */
    void publish(BridgeChannel channel, Object message);

    /**
     * Unsubscribes from a specific channel and removes its registered handlers.
     *
     * <p>특정 채널의 구독을 취소하고 등록된 핸들러들을 제거한다.
     *
     * @param channel channel to unsubscribe
     */
    void unsubscribe(BridgeChannel channel);

    /**
     * Closes and cleans up all connections and channel communication for this bridge.
     *
     * <p>이 브릿지의 모든 연결과 채널 통신을 닫고 정리한다.
     */
    void close();
}
