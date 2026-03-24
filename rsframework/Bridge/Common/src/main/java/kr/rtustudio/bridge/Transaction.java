package kr.rtustudio.bridge;

import kr.rtustudio.bridge.context.RequestContext;
import kr.rtustudio.bridge.context.ResponseContext;

import java.time.Duration;

/**
 * Transaction (RPC) bridge interface for point-to-point request-response communication. Each
 * request is sent to a specific {@link Node} and expects a response.
 *
 * <p>1:1 요청-응답 통신을 위한 트랜잭션(RPC) 브릿지 인터페이스. 각 요청은 특정 {@link Node}에 전송되고 응답을 기다린다.
 */
public interface Transaction extends Bridge {

    /**
     * Sends a single RPC request to the target node. Chain response handlers via {@link
     * RequestContext#on(Class, java.util.function.BiConsumer)} on the returned context.
     *
     * <p>대상 노드로 단일 RPC 요청을 전송한다. 반환된 {@link RequestContext}를 통해 응답 핸들러를 체이닝할 수 있다.
     *
     * @param target target node
     * @param channel bridge channel
     * @param request payload to send
     * @param timeout response wait timeout
     * @param <T> request packet type
     * @return RequestContext for chaining response handlers
     */
    <T> RequestContext request(Node target, BridgeChannel channel, T request, Duration timeout);

    /**
     * Returns a response handler registrar for a specific channel. Re-registering the same type
     * replaces the existing handler.
     *
     * <p>특정 채널에 대한 응답 핸들러 등록기를 반환한다. 동일 타입에 대해 다시 등록하면 기존 핸들러가 교체된다.
     *
     * @param channel bridge channel
     * @return handler registrar
     */
    ResponseContext respond(BridgeChannel channel);

    /**
     * Returns the default RPC request timeout.
     *
     * <p>RPC 요청의 기본 타임아웃 시간을 반환한다.
     *
     * @return configured default timeout
     */
    Duration getRequestTimeout();
}
