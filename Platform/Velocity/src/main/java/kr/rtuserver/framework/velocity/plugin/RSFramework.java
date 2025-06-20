package kr.rtuserver.framework.velocity.plugin;

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
    private final Path dir;
    private Libraries libraries;
    private VelocityProtoWeaver protoWeaver;

    @Inject
    public RSFramework(ProxyServer server, @DataDirectory Path dir) {
        this.server = server;
        this.dir = dir;
        this.libraries = new Libraries(this, log, dir.getParent().resolve("RSFramework"), server.getPluginManager());
        log.info("RSFramework Velocity loaded.");
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        libraries.load("org.apache.commons:commons-lang3:3.14.0");
        libraries.load("com.google.code.gson:gson:2.10.1");
        libraries.load("com.google.guava:guava:32.1.2-jre");
        libraries.load("org.xerial.snappy:snappy-java:1.1.10.5");

        libraries.load("io.netty:netty-buffer:4.1.111.Final");
        libraries.load("io.netty:netty-transport:4.1.111.Final");
        libraries.load("io.netty:netty-handler:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http2:4.1.111.Final");
        libraries.load("org.apache.fury:fury-core:0.10.3", "org.apache.fury", "kr.rtuserver.protoweaver.fury");
        libraries.load("org.bouncycastle:bcpkix-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcprov-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcutil-jdk18on:1.80");
        libraries.load("org.javassist:javassist:3.30.2-GA");
        libraries.load("org.reflections:reflections:0.10.2");

        protoWeaver = new kr.rtuserver.protoweaver.core.impl.velocity.VelocityProtoWeaver(server, dir.toAbsolutePath().getParent().getParent());
        server.getEventManager().register(this, protoWeaver);
    }

}
