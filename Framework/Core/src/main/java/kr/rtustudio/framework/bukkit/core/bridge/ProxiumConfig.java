package kr.rtustudio.framework.bukkit.core.bridge;

import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;
import kr.rtustudio.configure.ConfigurationPart;
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
public class ProxiumConfig extends ConfigurationPart {
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
