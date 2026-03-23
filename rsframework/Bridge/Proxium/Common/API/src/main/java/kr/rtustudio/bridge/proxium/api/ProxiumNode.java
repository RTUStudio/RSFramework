package kr.rtustudio.bridge.proxium.api;

import java.net.InetSocketAddress;

import org.jspecify.annotations.NonNull;

/**
 * Server information registered in the Proxium network.
 *
 * <p>Proxium 네트워크에 등록된 서버 정보.
 */
public record ProxiumNode(@NonNull String name, @NonNull String host, int port) {

    /**
     * Returns the address string in {@code "host:port"} format.
     *
     * <p>"host:port" 형식의 주소 문자열을 반환한다.
     */
    public String getAddress() {
        return host + ":" + port;
    }

    /**
     * Returns an {@link InetSocketAddress} for this node.
     *
     * <p>InetSocketAddress 객체를 반환한다.
     */
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }
}
