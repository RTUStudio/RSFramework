package kr.rtustudio.bridge.context;

import kr.rtustudio.bridge.exception.RequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Chaining builder for handling RPC request responses by type.
 *
 * <p>RPC 요청에 대한 응답을 타입별로 처리할 수 있는 체이닝 빌더.
 *
 * <pre>{@code
 * proxium.request("Survival-1", channel, new BalanceRequest(uuid), Duration.ofSeconds(5))
 *     .on(BalanceResponse.class, (sender, response) -> player.sendMessage("Balance: " + response.balance()))
 *     .error(e -> player.sendMessage("Request failed: " + e.type()));
 * }</pre>
 */
public class RequestContext {

    private final CompletableFuture<Object[]> future;

    /**
     * @param future future completing with [0]=sender(String), [1]=payload(Object)
     */
    public RequestContext(CompletableFuture<Object[]> future) {
        this.future = future;
    }

    /**
     * Registers a handler for a specific response type. The type must be registered on the channel
     * beforehand.
     *
     * <p>특정 응답 타입에 대한 핸들러를 등록한다. 응답 타입은 사전에 채널에 등록되어 있어야 한다.
     *
     * @param type expected response type class
     * @param handler callback for (sender, response)
     * @param <R> response type
     * @return this context for chaining
     */
    public <R> RequestContext on(Class<R> type, BiConsumer<String, R> handler) {
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
     * Registers an error handler for failed RPC requests.
     *
     * <p>RPC 요청이 실패했을 때의 에러 핸들러를 등록한다.
     *
     * @param handler callback for RequestException
     * @return this context for chaining
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
                                            ResponseStatus.TIMEOUT,
                                            "Transaction request timed out",
                                            te));
                        }
                    }
                });
        return this;
    }

    /**
     * Returns the internal CompletableFuture with type filtering applied. The response type must be
     * registered beforehand.
     *
     * <p>내부 CompletableFuture를 타입 캐스팅하여 반환한다. 응답 타입은 사전에 채널에 등록되어 있어야 한다.
     *
     * @param type response type class
     * @param <R> response type
     * @return type-filtered CompletableFuture
     */
    public <R> CompletableFuture<R> asFuture(Class<R> type) {
        return future.thenApply(result -> type.cast(result[1]));
    }
}
