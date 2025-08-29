package kr.rtuserver.protoweaver.bukkit.core.nms.v1_21_r4;

import io.papermc.paper.configuration.GlobalConfiguration;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import kr.rtuserver.protoweaver.bukkit.api.nms.IProtoWeaver;
import kr.rtuserver.protoweaver.core.loader.netty.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RSF/ProtoWeaver")
@RequiredArgsConstructor
public class ProtoWeaver_1_21_R4 implements IProtoWeaver {

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
        boolean enabled = GlobalConfiguration.get().proxies.velocity.enabled;
        if (!enabled) return false;
        String secret = GlobalConfiguration.get().proxies.velocity.secret;
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
