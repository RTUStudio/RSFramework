package kr.rtustudio.bridge;

import java.util.function.Consumer;

/**
 * Broadcast (Pub/Sub) bridge interface for channel-based message distribution. All nodes
 * subscribing to a channel receive published messages.
 *
 * <p>채널 기반 메시지 배포를 위한 브로드캐스트(Pub/Sub) 브릿지 인터페이스. 채널을 구독하는 모든 노드가 발행된 메시지를 수신한다.
 */
public interface Broadcast extends Bridge {

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
}
