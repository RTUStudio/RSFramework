package kr.rtustudio.bridge.context;

/**
 * Status codes for RPC response outcomes.
 *
 * <p>RPC 응답 결과 상태 코드.
 */
public enum ResponseStatus {
    /** Request processed successfully. / 요청이 성공적으로 처리됨. */
    SUCCESS,

    /**
     * No ResponseHandler found on the target server for the channel. / 대상 서버에서 해당 채널의 핸들러를 찾을 수 없음.
     */
    NO_HANDLER,

    /** Internal server exception during request processing. / 요청 처리 중 서버 내부 예외 발생. */
    ERROR,

    /** Response not received due to timeout (generated locally). / 타임아웃으로 응답 미수신 (로컬 생성). */
    TIMEOUT
}
