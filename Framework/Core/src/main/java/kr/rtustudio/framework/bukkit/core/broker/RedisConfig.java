package kr.rtustudio.framework.bukkit.core.broker;

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

    @Comment("Redis server connection settings")
    public Connection connection;

    @Comment("Redis TLS settings")
    public Tls tls;

    @Comment("Redis Sentinel settings")
    public Sentinel sentinel;

    @Comment("Redis Cluster settings")
    public Cluster cluster;

    public kr.rtustudio.broker.redis.config.RedisConfig toRedisConfig() {
        kr.rtustudio.broker.redis.config.RedisConfig.RedisConfigBuilder builder =
                kr.rtustudio.broker.redis.config.RedisConfig.builder()
                        .host(connection.host)
                        .port(connection.port)
                        .database(connection.database)
                        .tls(tls.enabled);

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

        @Comment("Redis server host")
        private String host = "127.0.0.1";

        @Comment("Redis server port")
        private int port = 6379;

        @Comment("Redis password (leave empty if none)")
        private String password = "";

        @Comment("Redis database index")
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

        @Comment("Enable Redis Sentinel mode")
        private boolean enabled = false;

        @Comment("Sentinel master name")
        private String masterName = "mymaster";

        @Comment("Sentinel node addresses (e.g. redis://127.0.0.1:26379)")
        private String[] addresses = {"redis://127.0.0.1:26379"};
    }

    @Getter
    public class Cluster extends ConfigurationPart {

        @Comment("Enable Redis Cluster mode")
        private boolean enabled = false;

        @Comment("Cluster node addresses (e.g. redis://127.0.0.1:7000)")
        private String[] addresses = {"redis://127.0.0.1:7000"};
    }
}
