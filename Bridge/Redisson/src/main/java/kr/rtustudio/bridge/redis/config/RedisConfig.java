package kr.rtustudio.bridge.redis.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RedisConfig {

    @Builder.Default private final boolean enabled = false;

    @Builder.Default private final String host = "127.0.0.1";

    @Builder.Default private final int port = 6379;

    private final String password;

    @Builder.Default private final int database = 0;

    @Builder.Default private final boolean tls = false;

    @Builder.Default private final boolean sentinel = false;

    private final String sentinelMasterName;
    private final String[] sentinelAddresses;

    @Builder.Default private final boolean cluster = false;

    private final String[] nodeAddresses;

    @Builder.Default private final long lockWaitTime = 3000;

    @Builder.Default private final long lockLeaseTime = 5000;

    public String getAddress() {
        return "redis://" + host + ":" + port;
    }
}
