package kr.rtustudio.framework.bukkit.core.broker;

import kr.rtustudio.broker.BrokerOptions;
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
public class ProtoWeaverConfig extends ConfigurationPart {
    @Comment("ProtoWeaver TLS settings")
    public Tls tls;

    @Comment(
            """
            Enable Snappy compression for broker messages
            브로커 메시지에 Snappy 압축을 사용합니다""")
    private boolean compression = false;

    public BrokerOptions toBrokerOptions(ClassLoader classLoader) {
        return BrokerOptions.builder(classLoader).tls(tls.enabled).compress(compression).build();
    }

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment(
                """
                Enable TLS for ProtoWeaver connections (Netty SslHandler)
                Self-signed certificate is auto-generated on first startup.
                ProtoWeaver 연결에 TLS를 활성화합니다 (Netty SslHandler)
                최초 실행 시 자체 서명 인증서가 자동으로 생성됩니다.""")
        private boolean enabled = true;
    }
}
