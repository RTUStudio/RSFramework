package kr.rtuserver.framework.bungee.plugin;

import kr.rtuserver.protoweaver.api.impl.bungee.BungeeProtoWeaver;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.plugin.Plugin;

@Slf4j(topic = "RSFramework")
public class RSFramework extends Plugin {

    private final Libraries libraries;
    private BungeeProtoWeaver protoWeaver;

    public RSFramework() {
        libraries = new Libraries(this);

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
    }

    @Override
    public void onEnable() {
        log.info("RSFramework Bungee loaded.");
        protoWeaver = new kr.rtuserver.protoweaver.core.impl.bungee.BungeeProtoWeaver(getProxy(), getDataFolder().toPath());
        getProxy().getPluginManager().registerListener(this, protoWeaver);
    }

    @Override
    public void onDisable() {
        protoWeaver.disable();
    }

}
