package kr.rtustudio.bridge;

import java.util.function.Consumer;

/** 프록시움과 레디스 등 다양한 네트워크 계층에서 사용되는 공통 브릿지 인터페이스. */
public interface Bridge {

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
     * 특정 채널에 메시지 타입별 구독 핸들러를 등록한다.
     *
     * <p>동일 채널에 여러 타입의 핸들러를 개별적으로 등록할 수 있다. 동일 타입에 대해 다시 호출하면 기존 핸들러가 교체된다.
     *
     * @param channel 구독할 채널
     * @param type 수신할 메시지 타입 클래스
     * @param handler 수신된 메시지를 처리할 콜백 함수
     * @param <T> 메시지 타입
     */
    <T> void subscribe(BridgeChannel channel, Class<T> type, Consumer<T> handler);

    /**
     * 특정 채널을 구독하고 있는 모든 노드에게 메시지를 발행(Broadcast)한다.
     *
     * @param channel 메시지를 발행할 채널
     * @param message 전송할 패킷 객체
     */
    void publish(BridgeChannel channel, Object message);

    /**
     * 특정 채널의 구독을 취소하고 등록된 핸들러들을 제거한다.
     *
     * @param channel 구독을 취소할 채널
     */
    void unsubscribe(BridgeChannel channel);

    /** 이 브릿지의 모든 연결과 채널 통신을 닫고 정리한다. */
    void close();
}
