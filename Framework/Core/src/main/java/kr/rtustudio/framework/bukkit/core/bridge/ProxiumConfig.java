package kr.rtustudio.framework.bukkit.core.bridge;

import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;
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
public class ProxiumConfig extends ConfigurationPart
        implements kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig {
    @Comment(
            """
                    TLS encryption settings for Proxium server-proxy connections
                    Proxium 서버-프록시 연결의 TLS 암호화 설정""")
    public Tls tls;

    @Comment(
            """
                    Compression algorithm for Proxium packets (NONE, GZIP, SNAPPY, FAST_LZ)
                    SNAPPY is recommended for low-latency environments
                    Proxium 패킷 압축 알고리즘 (NONE, GZIP, SNAPPY, FAST_LZ)
                    저지연 환경에는 SNAPPY 권장""")
    private CompressionType compression = CompressionType.SNAPPY;

    @Comment(
            """
                    Maximum allowed packet size in bytes
                    Packets exceeding this limit will be rejected
                    허용되는 최대 패킷 크기 (바이트)
                    이 크기를 초과하는 패킷은 거부됩니다""")
    private int maxPacketSize = 67108864;

    @Comment(
            """
                    Maximum number of connection retry attempts
                    Set to a high value (like 9223372036854775807) for infinite background retries
                    프록시의 최대 연결 재시도 횟수
                    백그라운드에서 무한히 재시도하려면 큰 값으로 설정하세요""")
    private long maxRetries = Long.MAX_VALUE;

    @Comment(
            """
                    Delay in seconds between connection retry attempts
                    프록시의 재시도 대기 간격 (초 단위)""")
    private long retryDelaySeconds = 5;

    @Getter
    public class Tls extends ConfigurationPart
            implements kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig.Tls {
        @Comment(
                """
                        Enable TLS encryption for Proxium connections
                        A self-signed certificate is auto-generated on first startup
                        Proxium 연결에 TLS 암호화를 활성화합니다
                        최초 실행 시 자체 서명 인증서가 자동 생성됩니다""")
        private boolean enabled = true;
    }
}
