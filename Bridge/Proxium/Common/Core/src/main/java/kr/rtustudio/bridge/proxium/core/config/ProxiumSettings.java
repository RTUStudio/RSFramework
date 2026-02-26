package kr.rtustudio.bridge.proxium.core.config;

import kr.rtustudio.bridge.BridgeOptions;
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
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "NotNullFieldNotInitialized",
    "InnerClassMayBeStatic"
})
public class ProxiumSettings extends ConfigurationPart {

    @Comment(
            """
                    Proxium TLS settings
                    Proxium TLS 설정""")
    public Tls tls;

    @Comment(
            """
                    Compression type for bridge messages (NONE, GZIP, SNAPPY, FAST_LZ)
                    브릿지 메시지 압축 방식 (NONE, GZIP, SNAPPY, FAST_LZ)""")
    private CompressionType compression = CompressionType.SNAPPY;

    @Comment(
            """
                    Maximum packet size in bytes (default: 64MB)
                    최대 패킷 크기 (바이트, 기본값: 64MB)""")
    private int maxPacketSize = 67108864;

    @Comment(
            """
                    Server reconnect poll rate in milliseconds
                    서버 재연결 폴링 주기 (밀리초)""")
    private int serverPollRate = 5000;

    /**
     * 데이터 폴더 경로에서 설정을 로드한다.
     *
     * @param dataFolder 플러그인 데이터 폴더 (예: {@code plugins/RSFramework})
     * @return 로드된 설정 인스턴스
     */
    public static ProxiumSettings load(Path dataFolder) {
        Path configPath = dataFolder.resolve("Config/Bridge/Proxium.yml");
        try {
            return new SimpleConfiguration<>(ProxiumSettings.class, configPath).load();
        } catch (Exception e) {
            log.error("Failed to load Proxium config, using defaults", e);
            return new ProxiumSettings();
        }
    }

    public BridgeOptions toBridgeOptions(ClassLoader classLoader) {
        return BridgeOptions.builder(classLoader).tls(tls.enabled).build();
    }

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment(
                """
                        Enable TLS for Proxium connections (Netty SslHandler)
                        Self-signed certificate is auto-generated on first startup.
                        Proxium 연결에 TLS를 활성화합니다 (Netty SslHandler)
                        최초 실행 시 자체 서명 인증서가 자동으로 생성됩니다.""")
        private boolean enabled = true;
    }
}
