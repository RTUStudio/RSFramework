package kr.rtustudio.bridge.proxium.api.exception;

/**
 * RPC 요청 처리 중 발생하는 예외.
 *
 * <p>요청 실패의 원인을 {@link ResponseStatus}로 분류하여 진단할 수 있다.
 */
public class RequestException extends RuntimeException {

    private final ResponseStatus status;

    public RequestException(ResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    public RequestException(ResponseStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /** 실패 원인을 나타내는 상태를 반환한다. */
    public ResponseStatus type() {
        return status;
    }

    /** 이 예외의 원인이 된 하위 예외를 반환한다. */
    public Throwable cause() {
        return getCause();
    }
}
