package kr.rtustudio.bridge.proxium.api.handler;

/**
 * 1:1 요청-응답 처리를 위한 핸들러 인터페이스.
 *
 * @param <T> 요청 타입
 * @param <R> 응답 타입
 */
@FunctionalInterface
public interface ResponseHandler<T, R> {

    /**
     * 요청을 처리하고 응답을 반환한다.
     *
     * @param sender 요청을 보낸 서버 이름
     * @param request 요청 객체
     * @return 처리가 완료된 결과 객체
     * @throws Exception 처리 도중 발생한 예외
     */
    R handle(String sender, T request) throws Exception;
}
