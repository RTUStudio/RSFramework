package kr.rtustudio.protoweaver.bukkit.core.nms.v1_18_r2;

import kr.rtustudio.protoweaver.api.util.ProtoLogger;
import kr.rtustudio.protoweaver.bukkit.api.nms.IProtoWeaver;
import kr.rtustudio.protoweaver.core.loader.netty.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.destroystokyo.paper.PaperConfig;

@Slf4j(topic = "RSF/ProtoWeaver")
@RequiredArgsConstructor
public class ProtoWeaver_1_18_R2 implements IProtoWeaver {

    private final String folder;

    public void setup() {
        ProtoLogger.setLogger(this);
        SSLContext.initKeystore(folder);
        SSLContext.genKeys();
        SSLContext.initContext();
        if (isModernProxy()) ModernProxy.initialize();
    }

    @Override
    public boolean isModernProxy() {
        if (!isPaper()) return false; // TODO: Fabric, Forge, Arclight 등의 Velocity 지원 확장을 고려해야함
        boolean enabled = PaperConfig.velocitySupport;
        if (!enabled) return false;
        String secret = new String(PaperConfig.velocitySecretKey);
        return secret != null && !secret.isEmpty();
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void err(String message) {
        log.error(message);
    }
}
