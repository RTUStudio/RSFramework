package kr.rtuserver.framework.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import kr.rtuserver.protoweaver.api.impl.velocity.VelocityProtoWeaver;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j(topic = "RSFramework")
public class RSFramework {

    private final ProxyServer server;
    private final VelocityProtoWeaver protoWeaver;

    @Inject
    public RSFramework(ProxyServer server, @DataDirectory Path dir) {
        this.server = server;
        log.info("RSFramework Velocity loaded.");
        protoWeaver = new kr.rtuserver.core.impl.velocity.VelocityProtoWeaver(server, dir.toAbsolutePath().getParent().getParent());
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        server.getEventManager().register(this, protoWeaver);
    }
}
