package kr.rtustudio.framework.bukkit.core.bridge;

import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@SuppressWarnings({
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "NotNullFieldNotInitialized",
    "InnerClassMayBeStatic"
})
public class RedisConfig extends ConfigurationPart {
    @Comment(
            """
                    Redis server connection settings
                    Redis 서버 연결 설정""")
    public Connection connection;

    @Comment(
            """
                    Redis TLS settings
                    Redis TLS 설정""")
    public Tls tls;

    @Comment(
            """
                    Redis Sentinel settings
                    Redis Sentinel 설정""")
    public Sentinel sentinel;

    @Comment(
            """
                    Redis Cluster settings
                    Redis Cluster 설정""")
    public Cluster cluster;

    @Comment(
            """
                    Redis Distributed Lock settings
                    Redis 분산 락 설정""")
    public Lock lock;

    @Comment(
            """
                    Enable Snappy compression for bridge messages
                    브릿지 메시지에 Snappy 압축을 사용합니다""")
    private boolean compression = false;

    public kr.rtustudio.bridge.redis.config.RedisConfig toRedisConfig() {
        kr.rtustudio.bridge.redis.config.RedisConfig.RedisConfigBuilder builder =
                kr.rtustudio.bridge.redis.config.RedisConfig.builder()
                        .host(connection.host)
                        .port(connection.port)
                        .database(connection.database)
                        .tls(tls.enabled)
                        .lockWaitTime(lock.waitTime)
                        .lockLeaseTime(lock.leaseTime);
        if (connection.password != null && !connection.password.isEmpty())
            builder.password(connection.password);
        if (sentinel.enabled)
            builder.sentinel(true)
                    .sentinelMasterName(sentinel.masterName)
                    .sentinelAddresses(sentinel.addresses);
        if (cluster.enabled) builder.cluster(true).nodeAddresses(cluster.addresses);
        return builder.build();
    }

    @Getter
    public class Connection extends ConfigurationPart {
        @Comment(
                """
                        Redis server host
                        Redis 서버 호스트 주소""")
        private String host = "127.0.0.1";

        @Comment(
                """
                        Redis server port
                        Redis 서버 포트""")
        private int port = 6379;

        @Comment(
                """
                        Redis password (leave empty if none)
                        Redis 비밀번호 (없을 경우 비워두세요)""")
        private String password = "";

        @Comment(
                """
                        Redis database index
                        Redis 데이터베이스 인덱스""")
        private int database = 0;
    }

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment(
                """
                        Enable TLS (rediss://) for Redis connection
                        Redis 서버에 TLS 연결을 사용합니다""")
        private boolean enabled = false;
    }

    @Getter
    public class Sentinel extends ConfigurationPart {
        @Comment(
                """
                        Enable Redis Sentinel mode
                        Redis Sentinel 모드를 사용합니다""")
        private boolean enabled = false;

        @Comment(
                """
                        Sentinel master name
                        Sentinel 마스터 이름""")
        private String masterName = "mymaster";

        @Comment(
                """
                        Sentinel node addresses (e.g. redis://127.0.0.1:26379)
                        Sentinel 노드 주소 목록 (예: redis://127.0.0.1:26379)""")
        private String[] addresses = {"redis://127.0.0.1:26379"};
    }

    @Getter
    public class Cluster extends ConfigurationPart {
        @Comment(
                """
                        Enable Redis Cluster mode
                        Redis Cluster 모드를 사용합니다""")
        private boolean enabled = false;

        @Comment(
                """
                        Cluster node addresses (e.g. redis://127.0.0.1:7000)
                        Cluster 노드 주소 목록 (예: redis://127.0.0.1:7000)""")
        private String[] addresses = {"redis://127.0.0.1:7000"};
    }

    @Getter
    public class Lock extends ConfigurationPart {
        @Comment(
                """
                        Maximum time to wait for the lock (ms)
                        락 획득을 위해 대기할 최대 시간 (밀리초)""")
        private long waitTime = 3000;

        @Comment(
                """
                        Maximum time to hold the lock before automatically releasing it (ms)
                        락 획득 후 자동으로 해제되기까지의 최대 시간 (밀리초)""")
        private long leaseTime = 5000;
    }
}
