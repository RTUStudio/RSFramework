package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.proxium.api.netty.Connection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal pipeline interface for Proxium. Provides methods for handshake, packet routing, and
 * connection management used only by the internal system. External plugin developers should use the
 * {@link Proxium} interface.
 *
 * <p>Proxium 내부 파이프라인 전용 인터페이스. 핸드셰이크, 패킷 라우팅, 커넥션 관리 등 내부 시스템에서만 사용되는 메서드를 제공한다. 외부 플러그인 개발자는
 * {@link Proxium} 인터페이스를 사용해야 한다.
 */
@ApiStatus.Internal
public interface ProxiumPipeline extends Proxium {

    /**
     * Sends an encoded binary packet directly to the connected proxy/backend. Always encoded as
     * {@code BridgeChannel.INTERNAL}. External use should call {@link #publish}.
     *
     * <p>연결된 프록시/백엔드 서버로 인코딩된 이진 패킷을 직접 전송한다. 항상 {@code BridgeChannel.INTERNAL}로 인코딩된다.
     *
     * @param packet packet object to send
     * @return whether send succeeded
     */
    boolean send(@NotNull Object packet);

    /**
     * Called by the internal system when a new connection has completed initial handshake.
     *
     * <p>새로운 커넥션이 초기 연결을 완료하고 통신이 준비되었을 때 내부 시스템에서 호출된다.
     *
     * @param connection the activated connection
     */
    void ready(Connection connection);
}
