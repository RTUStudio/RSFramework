package kr.rtustudio.bridge.proxium.bukkit.core;

import kr.rtustudio.bridge.proxium.core.netty.SSLContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Proxium")
public class Security {

    private final String sslFolder;
    @Getter private boolean modernProxy = false;

    public Security(String sslFolder) {
        this.sslFolder = sslFolder;
    }

    public void setup(boolean tls) {
        if (isPaper() && isVelocityForwardingEnabled()) {
            try {
                modernProxy = true;
                ModernProxy.initialize();
            } catch (Exception e) {
                log.warn("Modern proxy detection failed, falling back to legacy mode");
                modernProxy = false;
            }
        }

        if (tls) {
            SSLContext.initKeystore(sslFolder);
            SSLContext.genKeys();
            SSLContext.initContext();
        }
    }

    public boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig")
                || hasClass("io.papermc.paper.configuration.Configuration");
    }

    private boolean isVelocityForwardingEnabled() {
        try {
            var config = io.papermc.paper.configuration.GlobalConfiguration.get();
            return config.proxies.velocity.enabled;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
