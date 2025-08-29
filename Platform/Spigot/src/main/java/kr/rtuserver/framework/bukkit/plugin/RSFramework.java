package kr.rtuserver.framework.bukkit.plugin;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.plugin.command.FrameworkCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public class RSFramework extends RSPlugin {

    @Getter private static RSFramework instance;
    @Getter private final Libraries libraries;
    private final Metrics metrics;

    public RSFramework() {
        libraries = new Libraries(this);

        libraries.load("net.kyori:adventure-text-minimessage:4.16.0");

        libraries.load("com.zaxxer:HikariCP:7.0.2");
        libraries.load("org.mongodb:bson:5.5.1");
        libraries.load("org.mongodb:mongodb-driver-sync:5.5.1");
        libraries.load("org.mongodb:mongodb-driver-core:5.5.1");
        libraries.load("com.mysql:mysql-connector-j:9.4.0");
        libraries.load("org.mariadb.jdbc:mariadb-java-client:3.5.5");
        // libraries.load("org.xerial:sqlite-jdbc:3.45.3.0");
        // libraries.load("org.postgresql:postgresql:42.7.3");

        libraries.load("org.apache.commons:commons-lang3:3.18.0");
        libraries.load("com.google.code.gson:gson:2.13.1");
        libraries.load("com.google.guava:guava:33.4.8-jre");
        libraries.load("org.xerial.snappy:snappy-java:1.1.10.8");
        libraries.load("org.quartz-scheduler:quartz:2.5.0");

        libraries.load("io.netty:netty-buffer:4.1.111.Final");
        libraries.load("io.netty:netty-transport:4.1.111.Final");
        libraries.load("io.netty:netty-handler:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http2:4.1.111.Final");
        libraries.load(
                "org.apache.fury:fury-core:0.10.3",
                "org.apache.fury",
                "kr.rtuserver.protoweaver.fury");
        libraries.load("org.bouncycastle:bcpkix-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcprov-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcutil-jdk18on:1.80");
        libraries.load("org.javassist:javassist:3.30.2-GA");

        List<String> list = new ArrayList<>();
        list.add("kr.rtuserver.framework.bukkit");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof RSPlugin) list.add(plugin.getClass().getPackageName());
        }
        LightDI.init(list.toArray(new String[0]));

        this.metrics = new Metrics(this, 27054);
    }

    @Override
    protected void initialize() {
        getFramework().load(this);
    }

    @Override
    protected void load() {
        instance = this;
    }

    @Override
    protected void enable() {
        getFramework().enable(this);
        registerCommand(new FrameworkCommand(this), true);
    }

    @Override
    protected void disable() {
        metrics.shutdown();
        getFramework().disable(this);
    }
}
