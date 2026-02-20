package kr.rtustudio.framework.bukkit.plugin;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.plugin.command.FrameworkCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public class RSFramework extends RSPlugin {

    @Getter private static RSFramework instance;
    private final Metrics metrics;

    public RSFramework() {
        loadLibrary("net.kyori:adventure-text-minimessage:4.24.0");
        loadLibrary("net.kyori:adventure-text-serializer-gson:4.24.0");

        loadLibrary("com.zaxxer:HikariCP:7.0.2");
        loadLibrary("org.mongodb:bson:5.5.1");
        loadLibrary("org.mongodb:mongodb-driver-sync:5.5.1");
        loadLibrary("org.mongodb:mongodb-driver-core:5.5.1");
        loadLibrary("com.mysql:mysql-connector-j:9.4.0");
        loadLibrary("org.mariadb.jdbc:mariadb-java-client:3.5.5");

        loadLibrary("org.apache.commons:commons-lang3:3.18.0");
        loadLibrary("com.google.code.gson:gson:2.13.1");
        loadLibrary("com.google.guava:guava:33.4.8-jre");
        loadLibrary("it.unimi.dsi:fastutil:8.5.18");
        loadLibrary("org.xerial.snappy:snappy-java:1.1.10.8");
        loadLibrary("org.quartz-scheduler:quartz:2.5.0");

        loadLibrary("io.netty:netty-buffer:4.1.111.Final");
        loadLibrary("io.netty:netty-transport:4.1.111.Final");
        loadLibrary("io.netty:netty-handler:4.1.111.Final");
        loadLibrary("io.netty:netty-codec-http:4.1.111.Final");
        loadLibrary("io.netty:netty-codec-http2:4.1.111.Final");
        loadLibrary(
                "org.apache.fory:fory-core:0.15.0", "org.apache.fory", "kr.rtustudio.broker.fory");
        loadLibrary("org.bouncycastle:bcpkix-jdk18on:1.80");
        loadLibrary("org.bouncycastle:bcprov-jdk18on:1.80");
        loadLibrary("org.bouncycastle:bcutil-jdk18on:1.80");
        loadLibrary("org.javassist:javassist:3.30.2-GA");

        List<String> list = new ArrayList<>();
        list.add("kr.rtustudio.framework.bukkit");
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
