package kr.rtustudio.bridge.proxium.api.exception;

public enum ResponseStatus {
    /** 요청이 성공적으로 처리되었을 때 */
    SUCCESS,

    /** 지정된 대상 서버에서 해당 채널의 ResponseHandler를 찾을 수 없을 때 */
    NO_HANDLER,

    /** 요청 처리 중 서버 내부에서 예외가 발생했을 때 */
    ERROR,

    /** 타임아웃 등으로 인해 응답을 받지 못했을 때 (로컬에서 발생) */
    TIMEOUT
}
