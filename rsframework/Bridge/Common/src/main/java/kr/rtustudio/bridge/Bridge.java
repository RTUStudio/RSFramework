package kr.rtustudio.bridge;

import java.util.function.Consumer;

/** 프록시움과 레디스 등 다양한 네트워크 계층에서 사용되는 공통 브릿지 인터페이스. */
public interface Bridge {

    /**
     * 이 브릿지의 고유 식별자(ID)를 반환한다.
     *
     * @return 브릿지 고유 ID (예: "Lobby-1", "Survival-1")
     */
    String getId();

    /**
     * 브릿지가 네트워크에 연결되어 활성화된 상태인지 확인한다.
     *
     * @return 연결 상태
     */
    boolean isConnected();

    /**
     * 특정 채널에 사용할 패킷 클래스들을 바인딩하여 등록한다.
     *
     * @param channel 메시지를 송수신할 채널
     * @param types 등록할 패킷 클래스들의 가변 인자
     */
    void register(BridgeChannel channel, Class<?>... types);

    /**
     * 특정 채널로 수신되는 브로드캐스트 메시지를 처리할 구독 핸들러를 등록한다.
     *
     * @param channel 구독할 채널
     * @param handler 수신된 메시지(Object)를 처리할 콜백 함수
     */
    void subscribe(BridgeChannel channel, Consumer<Object> handler);

    /**
     * 특정 채널을 구독하고 있는 모든 노드에게 메시지를 발행(Broadcast)한다.
     *
     * @param channel 메시지를 발행할 채널
     * @param message 전송할 패킷 객체
     */
    void publish(BridgeChannel channel, Object message);

    /**
     * 특정 채널에서 지정한 타입의 메시지만 필터링하여 수신하는 구독을 등록한다.
     *
     * <p>한 채널에 여러 타입별 핸들러를 등록할 수 있으며, 타입 캐스팅 없이 안전하게 사용할 수 있다.
     *
     * @param channel 구독할 채널
     * @param type 수신할 메시지 타입 클래스
     * @param handler 수신된 메시지를 처리할 콜백 함수
     * @param <T> 메시지 타입
     */
    default <T> void subscribe(BridgeChannel channel, Class<T> type, Consumer<T> handler) {
        register(channel, type);
        subscribe(
                channel,
                packet -> {
                    if (type.isInstance(packet)) {
                        handler.accept(type.cast(packet));
                    }
                });
    }

    /**
     * 특정 채널의 구독을 취소하고 등록된 핸들러들을 제거한다.
     *
     * @param channel 구독을 취소할 채널
     */
    void unsubscribe(BridgeChannel channel);

    /** 이 브릿지의 모든 연결과 채널 통신을 닫고 정리한다. */
    void close();
}
