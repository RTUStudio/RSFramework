package kr.rtustudio.bridge.context;

import kr.rtustudio.bridge.exception.RequestException;
import kr.rtustudio.bridge.handler.ResponseHandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Chaining registrar for response handlers on a specific channel.
 *
 * <p>특정 채널에 대한 응답 핸들러를 체이닝 방식으로 등록하는 등록기.
 *
 * <pre>{@code
 * proxium.respond(channel)
 *     .on(BalanceRequest.class, (sender, req) -> new BalanceResponse(getBalance(req.uuid())))
 *     .on(TransferRequest.class, (sender, req) -> new TransferResponse(transfer(req)))
 *     .error(e -> log.error("Response failed: {}", e.type(), e));
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
     * Registers a response handler for a specific request type. The request type is automatically
     * registered on the channel.
     *
     * <p>특정 요청 타입에 대한 응답 핸들러를 등록한다. 요청 타입은 자동으로 채널에 등록된다.
     *
     * @param type request type class to receive
     * @param handler callback handler for processing requests
     * @param <T> request type
     * @param <R> response type
     * @return this context for chaining
     */
    public <T, R> ResponseContext on(Class<T> type, ResponseHandler<T, R> handler) {
        handlerCallback.accept(type, handler);
        return this;
    }

    /**
     * Registers an error handler for exceptions during response processing.
     *
     * <p>응답 핸들러 실행 중 발생한 예외를 처리할 에러 핸들러를 등록한다.
     *
     * @param handler callback for RequestException
     * @return this context for chaining
     */
    public ResponseContext error(Consumer<RequestException> handler) {
        errorCallback.accept(handler);
        return this;
    }
}
