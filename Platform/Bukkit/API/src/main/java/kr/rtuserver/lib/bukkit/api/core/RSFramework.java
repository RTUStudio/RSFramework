package kr.rtuserver.lib.bukkit.api.core;

import kr.rtuserver.lib.bukkit.api.RSPlugin;
import kr.rtuserver.lib.bukkit.api.command.RSCommand;
import kr.rtuserver.lib.bukkit.api.core.config.CommonTranslation;
import kr.rtuserver.lib.bukkit.api.core.internal.runnable.CommandLimit;
import kr.rtuserver.lib.bukkit.api.core.modules.Modules;
import kr.rtuserver.lib.bukkit.api.listener.RSListener;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.impl.bukkit.BukkitProtoWeaver;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

public interface RSFramework {

    Component getPrefix();

    Map<String, RSPlugin> getPlugins();

    Map<String, Boolean> getHooks();

    kr.rtuserver.lib.bukkit.api.nms.NMS getNMS();

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

    void registerEvent(RSListener listener);

    void registerCommand(RSCommand command);

    void registerPermission(String name, PermissionDefault permissionDefault);

    void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

}
