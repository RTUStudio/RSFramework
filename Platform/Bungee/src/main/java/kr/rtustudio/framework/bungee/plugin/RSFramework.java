package kr.rtustudio.framework.bungee.plugin;

import kr.rtustudio.bridge.protoweaver.bungee.api.ProtoWeaver;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.plugin.Plugin;

@Slf4j(topic = "RSFramework")
@SuppressWarnings("unused")
public class RSFramework extends Plugin {

    private final Libraries libraries;
    private ProtoWeaver protoWeaver;

    public RSFramework() {
        libraries = new Libraries(this);

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
        libraries.load("io.netty:netty-resolver-dns-classes-macos:4.1.111.Final");
        libraries.load("io.netty:netty-resolver-dns-native-macos:4.1.111.Final:osx-x86_64");
        libraries.load("io.netty:netty-resolver-dns-native-macos:4.1.111.Final:osx-aarch_64");

        // Fory
        libraries.load(
                "org.apache.fory:fory-core:0.15.0", "org.apache.fory", "kr.rtustudio.bridge.fory");

        // BouncyCastle
        libraries.load("org.bouncycastle:bcprov-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcutil-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcpkix-jdk18on:1.80");
    }

    @Override
    public void onEnable() {
        protoWeaver =
                new kr.rtustudio.bridge.protoweaver.bungee.core.ProtoWeaver(
                        getProxy(), getDataFolder().toPath());
        getProxy().getPluginManager().registerListener(this, protoWeaver);

        log.info("RSFramework Bungee loaded.");
    }

    @Override
    public void onDisable() {
        protoWeaver.shutdown();
    }
}
