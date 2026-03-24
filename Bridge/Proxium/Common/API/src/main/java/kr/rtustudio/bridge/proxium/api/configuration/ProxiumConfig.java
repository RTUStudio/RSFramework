package kr.rtustudio.bridge.proxium.api.configuration;

import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;

import java.time.Duration;

/** Proxium 구성 인터페이스. */
public interface ProxiumConfig {

    Tls getTls();

    CompressionType getCompression();

    int getMaxPacketSize();

    /** 트랜잭션 요청의 기본 타임아웃 시간을 반환한다. */
    default Duration getRequestTimeout() {
        return Duration.ofSeconds(5);
    }

    /** 프록시가 대상 서버에 연결을 재시도할 최대 횟수를 반환한다. */
    default long getMaxRetries() {
        return Long.MAX_VALUE;
    }

    /** 프록시가 서버에 연결을 재시도할 간격(초)을 반환한다. */
    default long getRetryDelaySeconds() {
        return 5;
    }

    interface Tls {
        boolean isEnabled();
    }
}
