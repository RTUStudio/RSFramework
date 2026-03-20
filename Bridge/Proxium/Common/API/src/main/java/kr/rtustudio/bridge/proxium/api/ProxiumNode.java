package kr.rtustudio.bridge.proxium.api;

import java.net.InetSocketAddress;

import org.jspecify.annotations.NonNull;

/** Proxium 네트워크에 등록된 서버 정보. */
public record ProxiumNode(@NonNull String name, @NonNull String host, int port) {

    /** "host:port" 형식의 주소 문자열을 반환한다. */
    public String getAddress() {
        return host + ":" + port;
    }

    /** InetSocketAddress 객체를 반환한다. */
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }
}
