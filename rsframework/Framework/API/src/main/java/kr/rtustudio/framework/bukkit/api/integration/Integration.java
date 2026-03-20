package kr.rtustudio.framework.bukkit.api.integration;

/**
 * 외부 플러그인 연동을 나타내는 인터페이스입니다.
 *
 * <p>연동 대상 플러그인의 존재 여부 확인, 등록/해제 라이프사이클을 정의합니다. {@link
 * kr.rtustudio.framework.bukkit.api.RSPlugin#registerIntegration(Integration)}으로 등록합니다.
 */
public interface Integration {

    /** 연동 대상 플러그인이 사용 가능한지 확인한다. */
    boolean isAvailable();

    /** 연동을 등록한다. */
    boolean register();

    /** 연동을 해제한다. */
    boolean unregister();

    /** 연동 내부 구현을 감싸는 래퍼 인터페이스. */
    interface Wrapper {

        boolean register();

        boolean unregister();
    }
}
