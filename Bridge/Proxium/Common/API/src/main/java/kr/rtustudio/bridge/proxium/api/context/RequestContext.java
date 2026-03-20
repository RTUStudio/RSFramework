package kr.rtustudio.bridge.proxium.api.context;

import kr.rtustudio.bridge.proxium.api.exception.RequestException;
import kr.rtustudio.bridge.proxium.api.exception.ResponseStatus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * RPC 요청에 대한 응답을 타입별로 처리할 수 있는 체이닝 빌더.
 *
 * <p>예시:
 *
 * <pre>{@code
 * proxium.request("Survival-1", channel, new BalanceRequest(uuid), Duration.ofSeconds(5))
 *     .on(BalanceResponse.class, (sender, response) -> player.sendMessage("잔고: " + response.balance()))
 *     .error(e -> player.sendMessage("요청 실패: " + e.type()));
 * }</pre>
 */
public class RequestContext {

    private final CompletableFuture<Object[]> future;
    private final Consumer<Class<?>> typeCallback;

    /**
     * @param future [0] = sender(String), [1] = payload(Object) 형태의 배열을 완료값으로 갖는 Future
     * @param typeCallback 응답 타입을 채널에 등록하는 함수
     */
    public RequestContext(CompletableFuture<Object[]> future, Consumer<Class<?>> typeCallback) {
        this.future = future;
        this.typeCallback = typeCallback;
    }

    /**
     * 특정 응답 타입에 대한 핸들러를 등록한다.
     *
     * <p>응답 타입은 자동으로 채널에 등록된다.
     *
     * @param type 기대하는 응답 타입 클래스
     * @param handler (sender, response) 를 처리할 콜백 함수
     * @param <R> 응답 타입
     * @return 체이닝을 위한 자기 자신
     */
    public <R> RequestContext on(Class<R> type, BiConsumer<String, R> handler) {
        typeCallback.accept(type);
        future.thenAccept(
                result -> {
                    Object payload = result[1];
                    if (type.isInstance(payload)) {
                        handler.accept((String) result[0], type.cast(payload));
                    }
                });
        return this;
    }

    /**
     * RPC 요청이 실패했을 때의 에러 핸들러를 등록한다.
     *
     * @param handler RequestException을 처리할 콜백 함수
     * @return 체이닝을 위한 자기 자신
     */
    public RequestContext error(Consumer<RequestException> handler) {
        future.whenComplete(
                (result, throwable) -> {
                    if (throwable != null) {
                        Throwable cause =
                                throwable instanceof CompletionException
                                        ? throwable.getCause()
                                        : throwable;
                        if (cause instanceof RequestException e) {
                            handler.accept(e);
                        } else if (cause instanceof TimeoutException te) {
                            handler.accept(
                                    new RequestException(
                                            ResponseStatus.TIMEOUT, "RPC request timed out", te));
                        }
                    }
                });
        return this;
    }

    /**
     * 내부 CompletableFuture를 타입 캐스팅하여 반환한다.
     *
     * <p>응답 타입은 자동으로 채널에 등록된다.
     *
     * @param type 응답 타입 클래스
     * @param <R> 응답 타입
     * @return 타입 필터링이 적용된 CompletableFuture
     */
    public <R> CompletableFuture<R> asFuture(Class<R> type) {
        typeCallback.accept(type);
        return future.thenApply(result -> type.cast(result[1]));
    }
}
