package kr.rtustudio.broker.redis.config;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for Redis connection. Supports single server, sentinel, and cluster modes.
 *
 * <p>When {@code tls} is {@code true}, the address scheme is automatically switched from {@code
 * redis://} to {@code rediss://} (TLS) by {@code BukkitRedission}.
 */
@Getter
@Builder
public class RedisConfig {

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

    /** Returns the assembled single-server address (e.g. {@code redis://127.0.0.1:6379}). */
    public String getAddress() {
        return "redis://" + host + ":" + port;
    }
}
