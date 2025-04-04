package kr.rtuserver.framework.bukkit.api.core;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.core.configuration.CommonTranslation;
import kr.rtuserver.framework.bukkit.api.core.internal.runnable.CommandLimit;
import kr.rtuserver.framework.bukkit.api.core.module.Modules;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import kr.rtuserver.framework.bukkit.api.nms.NMS;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.impl.bukkit.BukkitProtoWeaver;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

public interface Framework {

    Component getPrefix();

    Map<String, RSPlugin> getPlugins();

    Map<String, Boolean> getHooks();

    NMS getNMS();

    BukkitProtoWeaver getProtoWeaver();

    String getNMSVersion();

    CommandLimit getCommandLimit();

    CommonTranslation getCommonTranslation();

    Modules getModules();

    void loadPlugin(RSPlugin plugin);

    void unloadPlugin(RSPlugin plugin);

    boolean isEnabledDependency(String dependencyName);

    void hookDependency(String dependencyName);

    void load(RSPlugin plugin);

    void enable(RSPlugin plugin);

    void disable(RSPlugin plugin);

    void registerEvent(RSListener<? extends RSPlugin> listener);

    void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload);

    void updateCommand();

    void registerPermission(String name, PermissionDefault permissionDefault);

    void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

}
