package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig;
import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;

import java.time.Duration;

/** 테스트 전용 ProxiumConfig 스텁. */
class StubProxiumConfig implements ProxiumConfig {

    @Override
    public Tls getTls() {
        return () -> false;
    }

    @Override
    public CompressionType getCompression() {
        return CompressionType.NONE;
    }

    @Override
    public int getMaxPacketSize() {
        return 1048576;
    }

    @Override
    public Duration getRequestTimeout() {
        return Duration.ofSeconds(5);
    }
}
