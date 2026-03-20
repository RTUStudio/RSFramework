package kr.rtustudio.bridge.proxium.api.context;

import kr.rtustudio.bridge.proxium.api.exception.RequestException;
import kr.rtustudio.bridge.proxium.api.handler.ResponseHandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 특정 채널에 대한 응답 핸들러를 체이닝 방식으로 등록하는 등록기.
 *
 * <p>예시:
 *
 * <pre>{@code
 * proxium.respond(channel)
 *     .on(BalanceRequest.class, (sender, req) -> new BalanceResponse(getBalance(req.uuid())))
 *     .on(TransferRequest.class, (sender, req) -> new TransferResponse(transfer(req)))
 *     .error(e -> log.error("응답 처리 실패: {}", e.type(), e));
 * }</pre>
 */
public class ResponseContext {

    private final BiConsumer<Class<?>, ResponseHandler<?, ?>> handlerCallback;
    private final Consumer<Consumer<RequestException>> errorCallback;

    public ResponseContext(
            BiConsumer<Class<?>, ResponseHandler<?, ?>> handlerCallback,
            Consumer<Consumer<RequestException>> errorCallback) {
        this.handlerCallback = handlerCallback;
        this.errorCallback = errorCallback;
    }

    /**
     * 특정 요청 타입에 대한 응답 핸들러를 등록한다.
     *
     * <p>요청 타입은 자동으로 채널에 등록된다.
     *
     * @param type 수신할 요청 타입 클래스
     * @param handler 요청을 받아 처리할 콜백 핸들러
     * @param <T> 수신할 요청 객체의 타입
     * @param <R> 반환할 응답 객체의 타입
     * @return 체이닝을 위한 자기 자신
     */
    public <T, R> ResponseContext on(Class<T> type, ResponseHandler<T, R> handler) {
        handlerCallback.accept(type, handler);
        return this;
    }

    /**
     * 응답 핸들러 실행 중 발생한 예외를 처리할 에러 핸들러를 등록한다.
     *
     * @param handler RequestException을 처리할 콜백 함수
     * @return 체이닝을 위한 자기 자신
     */
    public ResponseContext error(Consumer<RequestException> handler) {
        errorCallback.accept(handler);
        return this;
    }
}
