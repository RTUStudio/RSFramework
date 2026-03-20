package kr.rtustudio.bridge.proxium.api.configuration;

import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;

import java.time.Duration;

/** Proxium 구성 인터페이스. */
public interface ProxiumConfig {

    Tls getTls();

    CompressionType getCompression();

    int getMaxPacketSize();

    /** RPC 요청의 기본 타임아웃 시간을 반환한다. */
    default Duration getRequestTimeout() {
        return Duration.ofSeconds(5);
    }

    interface Tls {
        boolean isEnabled();
    }
}
