package kr.rtustudio.framework.velocity.plugin;

import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.velocity.VelocityProxium;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

@Slf4j(topic = "RSFramework")
@SuppressWarnings("unused")
public class RSFramework {

    private final ProxyServer server;
    private final Path dir;
    private final Libraries libraries;

    private Proxium proxium;

    @Inject
    public RSFramework(ProxyServer server, @DataDirectory Path dir) {
        this.server = server;
        this.dir = dir;
        this.libraries =
                new Libraries(
                        this,
                        log,
                        dir.getParent().resolve("RSFramework"),
                        server.getPluginManager());
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        // Utilities
        libraries.load("com.google.code.gson:gson:2.13.1");
        libraries.load("com.google.guava:guava:33.4.8-jre");
        libraries.load("org.apache.commons:commons-lang3:3.18.0");
        libraries.load("org.xerial.snappy:snappy-java:1.1.10.8");
        libraries.load("it.unimi.dsi:fastutil:8.5.18");

        // Netty
        libraries.load("io.netty:netty-buffer:4.1.111.Final");
        libraries.load("io.netty:netty-transport:4.1.111.Final");
        libraries.load("io.netty:netty-handler:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http2:4.1.111.Final");

        // Fory
        libraries.load(
                "org.apache.fory:fory-core:0.15.0", "org.apache.fory", "kr.rtustudio.bridge.fory");

        // BouncyCastle
        libraries.load("org.bouncycastle:bcprov-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcutil-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcpkix-jdk18on:1.80");

        proxium = new VelocityProxium(server, dir.toAbsolutePath().getParent().getParent());
        server.getEventManager().register(this, proxium);

        log.info("RSFramework Velocity loaded.");
    }
}
