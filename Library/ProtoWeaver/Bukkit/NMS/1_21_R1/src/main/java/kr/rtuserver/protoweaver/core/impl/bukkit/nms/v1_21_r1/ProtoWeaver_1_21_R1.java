package kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_21_r1;

import io.papermc.paper.configuration.GlobalConfiguration;
import kr.rtuserver.protoweaver.api.impl.bukkit.nms.IProtoWeaver;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import kr.rtuserver.protoweaver.core.loader.netty.SSLContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RSF/ProtoWeaver")
public class ProtoWeaver_1_21_R1 implements IProtoWeaver {

    public ProtoWeaver_1_21_R1(String folder) {
        ProtoLogger.setLogger(this);
        SSLContext.initKeystore(folder);
        SSLContext.genKeys();
        SSLContext.initContext();
        if (isModernProxy()) ModernProxy.initialize();
    }

    @Override
    public boolean isModernProxy() {
        if (!isPaper()) return false; //TODO: Fabric, Forge, Arclight 등의 Velocity 지원 확장을 고려해야함
        boolean enabled = GlobalConfiguration.get().proxies.velocity.enabled;
        if (!enabled) return false;
        String secret = GlobalConfiguration.get().proxies.velocity.secret;
        if (secret == null || secret.isEmpty()) return false;
        return true;
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
