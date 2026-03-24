package kr.rtustudio.bridge.handler;

/**
 * Handler interface for 1:1 request-response processing.
 *
 * <p>1:1 요청-응답 처리를 위한 핸들러 인터페이스.
 *
 * @param <T> request type
 * @param <R> response type
 */
@FunctionalInterface
public interface ResponseHandler<T, R> {

    /**
     * Processes the request and returns a response.
     *
     * <p>요청을 처리하고 응답을 반환한다.
     *
     * @param sender name of the server that sent the request
     * @param request request object
     * @return processed result object
     * @throws Exception if processing fails
     */
    R handle(String sender, T request) throws Exception;
}
