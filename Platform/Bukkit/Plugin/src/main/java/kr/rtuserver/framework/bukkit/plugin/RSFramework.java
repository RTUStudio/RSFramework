package kr.rtuserver.framework.bukkit.plugin;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.core.listeners.*;
import kr.rtuserver.framework.bukkit.plugin.commands.FrameworkCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class RSFramework extends RSPlugin {

    @Getter
    private static RSFramework instance;
    @Getter
    private final Libraries libraries;

    public RSFramework() {
        libraries = new Libraries(this);

        libraries.load("net.kyori:adventure-text-minimessage:4.16.0");

        libraries.load("org.apache.commons:commons-lang3:3.14.0");
        libraries.load("com.google.code.gson:gson:2.10.1");
        libraries.load("com.google.guava:guava:32.1.2-jre");
        libraries.load("org.xerial.snappy:snappy-java:1.1.10.5");
        libraries.load("org.quartz-scheduler:quartz:2.5.0-rc1");

        libraries.load("com.zaxxer:HikariCP:5.1.0");
        libraries.load("org.mongodb:bson:4.11.1");
        libraries.load("org.mongodb:mongodb-driver-sync:4.11.1");
        libraries.load("com.mysql:mysql-connector-j:9.1.0");
        libraries.load("org.xerial:sqlite-jdbc:3.45.3.0");
        libraries.load("org.mariadb.jdbc:mariadb-java-client:3.3.3");
        libraries.load("org.postgresql:postgresql:42.7.3");

        libraries.load("io.netty:netty-codec-http:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http2:4.1.111.Final");
        libraries.load("org.apache.fury:fury-core:0.9.0");
        libraries.load("org.bouncycastle:bcpkix-jdk18on:1.79");
        libraries.load("org.bouncycastle:bcprov-jdk18on:1.79");
        libraries.load("org.bouncycastle:bcutil-jdk18on:1.79");
        libraries.load("org.javassist:javassist:3.30.2-GA");
        libraries.load("org.reflections:reflections:0.10.2");

        List<String> list = new ArrayList<>();
        list.add("kr.rtuserver.framework.bukkit");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof RSPlugin) list.add(plugin.getClass().getPackageName());
        }
        LightDI.init(list.toArray(new String[0]));
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

        registerPermission(getName() + ".motd", PermissionDefault.OP);
        registerPermission(getName() + ".broadcast", PermissionDefault.OP);
        registerPermission(getName() + ".information", PermissionDefault.OP);

        pluginItemListener();

        registerCommand(new FrameworkCommand(this));
    }

    private void pluginItemListener() {
        boolean ItemsAdder = getFramework().isEnabledDependency("ItemsAdder");
        boolean MMOItems = getFramework().isEnabledDependency("MMOItems");
        boolean Oraxen = getFramework().isEnabledDependency("Oraxen");
        boolean Nexo = getFramework().isEnabledDependency("Nexo");
        if (ItemsAdder) registerEvent(new ItemsAdderLoaded(this));
        if (MMOItems) registerEvent(new MMOItemsLoaded(this));
        if (Oraxen) registerEvent(new NexoLoaded(this));
        if (Nexo) registerEvent(new OraxenLoaded(this));
        if (!(ItemsAdder || MMOItems || Oraxen || Nexo)) registerEvent(new VanillaServerLoaded(this));
    }

    @Override
    protected void disable() {
        getFramework().disable(this);
    }
}
