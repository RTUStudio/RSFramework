package kr.rtustudio.framework.bukkit.api.integration;

/**
 * Interface for external plugin integrations. Defines availability check and register/unregister
 * lifecycle. Register with {@link
 * kr.rtustudio.framework.bukkit.api.RSPlugin#registerIntegration(Integration)}.
 *
 * <p>외부 플러그인 연동을 나타내는 인터페이스. 연동 대상 플러그인의 존재 여부 확인, 등록/해제 라이프사이클을 정의한다.
 */
public interface Integration {

    /** Checks if the target plugin is available. / 연동 대상 플러그인이 사용 가능한지 확인한다. */
    boolean isAvailable();

    /** Registers the integration. / 연동을 등록한다. */
    boolean register();

    /** Unregisters the integration. / 연동을 해제한다. */
    boolean unregister();

    /** Wrapper interface for the internal integration implementation. / 연동 내부 구현을 감싸는 래퍼 인터페이스. */
    interface Wrapper {

        boolean register();

        boolean unregister();
    }
}
