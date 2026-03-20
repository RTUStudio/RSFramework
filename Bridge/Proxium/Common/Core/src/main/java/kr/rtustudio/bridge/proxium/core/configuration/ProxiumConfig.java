package kr.rtustudio.bridge.proxium.core.configuration;

import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;
import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import org.spongepowered.configurate.objectmapping.meta.Comment;

/**
 * 프록시 측 Proxium 설정 ({@code Config/Bridge/Proxium.yml}).
 *
 * <p>TLS, 압축, 최대 패킷 크기, 서버 폴링 주기 등을 관리한다.
 */
@Slf4j(topic = "Proxium")
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
            Default timeout in milliseconds for RPC requests
            Used when no explicit timeout is specified in request() calls
            RPC 요청의 기본 타임아웃 (밀리초)
            request() 호출 시 타임아웃을 명시하지 않으면 이 값이 사용됩니다""")
    private long requestTimeout = 5000;

    @Override
    public java.time.Duration getRequestTimeout() {
        return java.time.Duration.ofMillis(requestTimeout);
    }

    /**
     * 데이터 폴더 경로에서 설정을 로드한다.
     *
     * @param dataFolder 플러그인 데이터 폴더 (예: {@code plugins/RSFramework})
     * @return 로드된 설정 인스턴스
     */
    public static ProxiumConfig load(Path dataFolder) {
        Path configPath = dataFolder.resolve("Config/Bridge/Proxium.yml");
        try {
            return new SimpleConfiguration<>(ProxiumConfig.class, configPath).load();
        } catch (Exception e) {
            log.error("Failed to load Proxium config, using defaults", e);
            return new ProxiumConfig();
        }
    }

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
