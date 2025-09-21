package kr.rtustudio.framework.bukkit.api.core;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.configuration.CommonTranslation;
import kr.rtustudio.framework.bukkit.api.core.internal.runnable.CommandLimit;
import kr.rtustudio.framework.bukkit.api.core.module.Modules;
import kr.rtustudio.framework.bukkit.api.core.provider.Providers;
import kr.rtustudio.framework.bukkit.api.core.scheduler.Scheduler;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.nms.NMS;
import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.protoweaver.api.protocol.Packet;
import kr.rtustudio.protoweaver.bukkit.api.BukkitProtoWeaver;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.Set;

import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public interface Framework {

    Component getPrefix();

    RSPlugin getPlugin();

    Map<String, RSPlugin> getPlugins();

    Map<String, Boolean> getHooks();

    NMS getNMS();

    @NotNull
    BukkitProtoWeaver getProtoWeaver();

    String getNMSVersion();

    CommandLimit getCommandLimit();

    CommonTranslation getCommonTranslation();

    Modules getModules();

    Providers getProviders();

    Scheduler getScheduler();

    void loadPlugin(RSPlugin plugin);

    void unloadPlugin(RSPlugin plugin);

    boolean isEnabledDependency(String dependencyName);

    void hookDependency(String dependencyName);

    void load(RSPlugin plugin);

    void enable(RSPlugin plugin);

    void disable(RSPlugin plugin);

    void registerEvent(RSListener<? extends RSPlugin> listener);

    void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload);

    void registerPermission(String name, PermissionDefault permissionDefault);

    void registerProtocol(
            String namespace,
            String key,
            Packet packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback);

    void registerProtocol(
            String namespace,
            String key,
            Set<Packet> packets,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback);
}
