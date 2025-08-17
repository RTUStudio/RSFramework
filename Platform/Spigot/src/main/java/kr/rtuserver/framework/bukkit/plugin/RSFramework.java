package kr.rtuserver.framework.bukkit.plugin;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.GlobalConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.RSConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.TlConfiguration;
import kr.rtuserver.framework.bukkit.plugin.command.FrameworkCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RSFramework extends RSPlugin {

    @Getter
    private static RSFramework instance;
    @Getter
    private final Libraries libraries;

    public RSFramework() {
        libraries = new Libraries(this);

        //libraries.load("net.kyori:adventure-text-minimessage:4.16.0");

        libraries.load("com.zaxxer:HikariCP:5.1.0");
        libraries.load("org.mongodb:bson:5.4.0");
        libraries.load("org.mongodb:mongodb-driver-sync:5.4.0");
        libraries.load("org.mongodb:mongodb-driver-core:5.4.0");
        libraries.load("com.mysql:mysql-connector-j:9.1.0");
        libraries.load("org.xerial:sqlite-jdbc:3.45.3.0");
        libraries.load("org.mariadb.jdbc:mariadb-java-client:3.3.3");
        libraries.load("org.postgresql:postgresql:42.7.3");

        libraries.load("org.apache.commons:commons-lang3:3.14.0");
        libraries.load("com.google.code.gson:gson:2.10.1");
        libraries.load("com.google.guava:guava:32.1.2-jre");
        libraries.load("org.xerial.snappy:snappy-java:1.1.10.5");
        libraries.load("org.quartz-scheduler:quartz:2.5.0-rc1");

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

        List<String> list = new ArrayList<>();
        list.add("kr.rtuserver.framework.bukkit");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof RSPlugin) list.add(plugin.getClass().getPackageName());
        }
        LightDI.init(list.toArray(new String[0]));

        RSConfiguration<RSFramework> rsc = new RSConfiguration<>(this);
        rsc.register(GlobalConfiguration.class, "Test", 1);
        rsc.register(TlConfiguration.class, "Translations");
        System.out.println(rsc.get(GlobalConfiguration.class));
        System.out.println(rsc.get(TlConfiguration.class).map.keySet().stream().map(objects -> {
            String[] result = new String[objects.length];
            for (int i = 0; i < objects.length; i++) {
                Object o = objects[i];
                if (o instanceof String) result[i] = "'" + o + "'";
                else result[i] = o.toString();
            }
            return Arrays.toString(result);
        }).toList());
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
        getFramework().disable(this);
    }

}
