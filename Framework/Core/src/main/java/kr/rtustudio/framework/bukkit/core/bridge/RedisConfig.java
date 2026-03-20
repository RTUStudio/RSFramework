package kr.rtustudio.framework.bukkit.core.bridge;

import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;

import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class RedisConfig extends ConfigurationPart {
    @Comment(
            """
            Enable the Redis bridge for cross-server data sharing
            서버 간 데이터 공유를 위한 Redis 연결을 활성화합니다""")
    public boolean enabled = false;

    @Comment(
            """
            Redis server connection settings (host, port, password, database)
            Redis 서버 연결 설정 (호스트, 포트, 비밀번호, 데이터베이스)""")
    public Connection connection;

    @Comment(
            """
            TLS encryption settings for secure Redis connections
            Redis 보안 연결을 위한 TLS 암호화 설정""")
    public Tls tls;

    @Comment(
            """
            Redis Sentinel settings for high availability
            고가용성을 위한 Redis Sentinel 설정""")
    public Sentinel sentinel;

    @Comment(
            """
            Redis Cluster settings for horizontal scaling
            수평 확장을 위한 Redis Cluster 설정""")
    public Cluster cluster;

    @Comment(
            """
            Distributed lock settings for concurrent access control
            동시 접근 제어를 위한 분산 락 설정""")
    public Lock lock;

    public kr.rtustudio.bridge.redis.config.RedisConfig toRedisConfig() {
        kr.rtustudio.bridge.redis.config.RedisConfig.RedisConfigBuilder builder =
                kr.rtustudio.bridge.redis.config.RedisConfig.builder()
                        .enabled(enabled)
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
                Redis server hostname or IP address
                Redis 서버 호스트명 또는 IP 주소""")
        private String host = "127.0.0.1";

        @Comment(
                """
                Redis server port
                Redis 서버 포트""")
        private int port = 6379;

        @Comment(
                """
                Redis authentication password (leave empty for no auth)
                Redis 인증 비밀번호 (인증 없이 사용하려면 비워두세요)""")
        private String password = "";

        @Comment(
                """
                Redis database index (0-15)
                Redis 데이터베이스 인덱스 (0-15)""")
        private int database = 0;
    }

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment(
                """
                Enable TLS encryption for Redis connections (rediss:// protocol)
                Redis TLS 암호화를 활성화합니다 (rediss:// 프로토콜)""")
        private boolean enabled = false;
    }

    @Getter
    public class Sentinel extends ConfigurationPart {
        @Comment(
                """
                Enable Redis Sentinel mode for automatic failover
                자동 장애 조치를 위한 Redis Sentinel 모드를 활성화합니다""")
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
                Enable Redis Cluster mode for distributed data sharding
                분산 데이터 샤딩을 위한 Redis Cluster 모드를 활성화합니다""")
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
                Maximum time to wait for lock acquisition (ms)
                락 획득을 위한 최대 대기 시간 (밀리초)""")
        private long waitTime = 3000;

        @Comment(
                """
                Maximum time to hold the lock before auto-release (ms)
                락 자동 해제까지의 최대 유지 시간 (밀리초)""")
        private long leaseTime = 5000;
    }
}
