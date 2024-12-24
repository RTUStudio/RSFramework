package kr.rtuserver.framework.bukkit.core;

import de.tr7zw.changeme.nbtapi.NBT;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.utility.platform.MinecraftVersion;
import kr.rtuserver.framework.bukkit.api.utility.platform.SystemEnvironment;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import kr.rtuserver.framework.bukkit.core.config.CommonTranslation;
import kr.rtuserver.framework.bukkit.core.internal.listeners.InventoryListener;
import kr.rtuserver.framework.bukkit.core.internal.listeners.JoinListener;
import kr.rtuserver.framework.bukkit.core.internal.runnable.CommandLimit;
import kr.rtuserver.framework.bukkit.core.modules.Modules;
import kr.rtuserver.framework.bukkit.nms.v1_19_r2.NMS_1_19_R2;
import kr.rtuserver.framework.bukkit.nms.v1_19_r3.NMS_1_19_R3;
import kr.rtuserver.framework.bukkit.nms.v1_20_r1.NMS_1_20_R1;
import kr.rtuserver.framework.bukkit.nms.v1_20_r2.NMS_1_20_R2;
import kr.rtuserver.framework.bukkit.nms.v1_20_r3.NMS_1_20_R3;
import kr.rtuserver.framework.bukkit.nms.v1_20_r4.NMS_1_20_R4;
import kr.rtuserver.framework.bukkit.nms.v1_21_r1.NMS_1_21_R1;
import kr.rtuserver.framework.bukkit.nms.v1_21_r2.NMS_1_21_R2;
import kr.rtuserver.framework.bukkit.nms.v1_21_r3.NMS_1_21_R3;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.internal.BroadcastChat;
import kr.rtuserver.protoweaver.api.protocol.internal.StorageSync;
import kr.rtuserver.protoweaver.core.impl.bukkit.BukkitProtoWeaver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "RSFramework")
@kr.rtuserver.cdi.annotations.Component
public class Framework implements kr.rtuserver.framework.bukkit.api.core.Framework {

    @Getter
    private final Component prefix = ComponentFormatter.mini("<gradient:#2979FF:#7C4DFF>RSFramework » </gradient>");
    @Getter
    private final Map<String, RSPlugin> plugins = new HashMap<>();
    @Getter
    private final Map<String, Boolean> hooks = new HashMap<>();
    @Getter
    private RSPlugin rsf;
    @Getter
    private kr.rtuserver.framework.bukkit.api.nms.NMS NMS;
    @Getter
    private BukkitProtoWeaver protoWeaver;
    private final HandlerCallback callback = new HandlerCallback(this::onReady, this::onPacket);
    @Getter
    private String NMSVersion;
    @Getter
    private CommandLimit commandLimit;
    @Getter
    private CommonTranslation commonTranslation;
    @Getter
    private Modules modules;

    public void loadPlugin(RSPlugin plugin) {
        log.info("loading RSPlugin: {}", plugin.getName());
        plugins.put(plugin.getName(), plugin);
    }

    public void unloadPlugin(RSPlugin plugin) {
        log.info("unloading RSPlugin: {}", plugin.getName());
        plugins.remove(plugin.getName());
    }

    private void onReady(HandlerCallback.Ready ready) {
        protoWeaver.onReady(ready);
    }

    private void onPacket(HandlerCallback.Packet packet) {
        protoWeaver.onPacket(packet);
        if (packet.packet() instanceof StorageSync sync) {
            RSPlugin plugin = plugins.get(sync.plugin());
            if (plugin == null) return;
            plugin.syncStorage(sync.name());
        }
        if (packet.packet() instanceof BroadcastChat chat) {
            PlayerChat.of(rsf).broadcast(chat.minimessage());
        }
    }

    public boolean isEnabledDependency(String dependencyName) {
        return hooks.getOrDefault(dependencyName, false);
    }

    public void hookDependency(String dependencyName) {
        hooks.put(dependencyName, Bukkit.getPluginManager().isPluginEnabled(dependencyName));
    }

    public void load(RSPlugin plugin) {
        this.rsf = plugin;
        if (!NBT.preloadApi()) {
            log.warn("NBT-API wasn't initialized properly, disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
        loadNMS(plugin);
        modules = new Modules(plugin);
    }

    private void loadNMS(RSPlugin plugin) {
        NMSVersion = MinecraftVersion.getNMS(MinecraftVersion.getAsText());
        switch (NMSVersion) {
            case "v1_19_R2" -> NMS = new NMS_1_19_R2();
            case "v1_19_R3" -> NMS = new NMS_1_19_R3();
            case "v1_20_R1" -> NMS = new NMS_1_20_R1();
            case "v1_20_R2" -> NMS = new NMS_1_20_R2();
            case "v1_20_R3" -> NMS = new NMS_1_20_R3();
            case "v1_20_R4" -> NMS = new NMS_1_20_R4();
            case "v1_21_R1" -> NMS = new NMS_1_21_R1();
            case "v1_21_R2" -> NMS = new NMS_1_21_R2();
            case "v1_21_R3" -> NMS = new NMS_1_21_R3();
            default -> {
                log.warn("Server version is unsupported version, Disabling RSFramework...");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
        protoWeaver = new BukkitProtoWeaver(plugin.getDataFolder().getPath(), NMSVersion, callback);
    }

    public void enable(RSPlugin plugin) {
        printStartUp(plugin);
        commonTranslation = new CommonTranslation(plugin);
        registerInternal(plugin);
    }

    public void disable(RSPlugin plugin) {
    }

    private void registerInternal(RSPlugin plugin) {
        registerInternalRunnable(plugin);
        registerInternalListener(plugin);
    }

    private void registerInternalRunnable(RSPlugin plugin) {
        commandLimit = new CommandLimit(plugin);
    }

    private void registerInternalListener(RSPlugin plugin) {
        registerEvent(new JoinListener(this, plugin));
        registerEvent(new InventoryListener(plugin));
    }

    private void printStartUp(RSPlugin plugin) {
        List<String> list = List.of(
                "╔ <gray>Developed by</gray> ════════════════════════════════════════════════════════════════════════════╗",
                "║ ░█▀▄░█░█░▀█▀░█▀█░█▀▀░█▀▄░░░▀█▀░█▀▀░█▀▀░█░█░█▀█░█▀█░█░░░█▀█░█▀▀░█░█░░░█░█░█▀█░▀█▀░█▀▀░█░█ ║",
                "║ ░█▀▄░█░█░░█░░█░█░█▀▀░█░█░░░░█░░█▀▀░█░░░█▀█░█░█░█░█░█░░░█░█░█░█░░█░░░░█░█░█░█░░█░░█▀▀░░█░ ║",
                "║ ░▀░▀░▀▀▀░▀▀▀░▀░▀░▀▀▀░▀▀░░░░░▀░░▀▀▀░▀▀▀░▀░▀░▀░▀░▀▀▀░▀▀▀░▀▀▀░▀▀▀░░▀░░░░▀▀▀░▀░▀░▀▀▀░▀░░░░▀░ ║",
                "╚══════════════════════════════════════════════════════════════════════════════════════════╝"
        );
        plugin.getAdventure().console().sendMessage(ComponentFormatter.mini(
                "RSFramework | Version: %s | Bukkit: %s | NMS: %s | OS: %s | JDK: %s"
                        .formatted(plugin.getDescription().getVersion()
                                , Bukkit.getName() + "-" + MinecraftVersion.getAsText()
                                , NMSVersion
                                , SystemEnvironment.getOS()
                                , SystemEnvironment.getJDKVersion())));
        for (String message : list)
            plugin.getAdventure().console().sendMessage(ComponentFormatter.mini("<gradient:#2979FF:#7C4DFF>" + message + "</gradient>"));
    }

    public void registerEvent(RSListener listener) {
        Bukkit.getPluginManager().registerEvents(listener, listener.getPlugin());
    }

    public void registerCommand(RSCommand command) {
        NMS.commandMap().register(command.getName(), command);
    }

    public void registerPermission(String name, PermissionDefault permissionDefault) {
        Bukkit.getPluginManager().addPermission(new Permission(name, permissionDefault));
    }

    public void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        protoWeaver.registerProtocol(namespace, key, packet, protocolHandler, callback);
    }
}
