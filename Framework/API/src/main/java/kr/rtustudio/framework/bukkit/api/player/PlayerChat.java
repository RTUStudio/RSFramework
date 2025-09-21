package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.protoweaver.api.protocol.internal.Broadcast;
import kr.rtustudio.protoweaver.api.protocol.internal.SendMessage;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.protoweaver.bukkit.api.BukkitProtoWeaver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerChat {

    static Framework framework;
    private final RSPlugin plugin;
    @Getter private Audience receiver;

    PlayerChat(RSPlugin plugin, Audience audience) {
        this.plugin = plugin;
        this.receiver = audience;
    }

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static PlayerChat of(RSPlugin plugin) {
        return new PlayerChat(plugin, null);
    }

    public static PlayerChat of(RSPlugin plugin, CommandSender sender) {
        return new PlayerChat(plugin, plugin.getAdventure().sender(sender));
    }

    public static PlayerChat of(RSPlugin plugin, Player player) {
        return new PlayerChat(plugin, plugin.getAdventure().player(player));
    }

    public static PlayerChat of(RSPlugin plugin, Audience audience) {
        return new PlayerChat(plugin, audience);
    }

    public static void broadcast(String minimessage) {
        broadcast(ComponentFormatter.mini(minimessage));
    }

    public static void broadcast(Component component) {
        framework().getPlugin().getAdventure().all().sendMessage(component);
    }

    public static void broadcast(Predicate<CommandSender> filter, String minimessage) {
        framework()
                .getPlugin()
                .getAdventure()
                .filter(filter)
                .sendMessage(ComponentFormatter.mini(minimessage));
    }

    public static void broadcast(Predicate<CommandSender> filter, Component component) {
        broadcast(filter, ComponentFormatter.mini(component));
    }

    public static void broadcastAll(String minimessage) {
        BukkitProtoWeaver pw = framework().getProtoWeaver();
        if (pw.isConnected()) {
            if (pw.sendPacket(new Broadcast(minimessage))) return;
        }
        broadcast(minimessage);
    }

    public static void broadcastAll(Component component) {
        BukkitProtoWeaver pw = framework().getProtoWeaver();
        if (pw.isConnected()) {
            if (pw.sendPacket(new Broadcast(ComponentFormatter.mini(component)))) return;
        }
        broadcast(component);
    }

    public static void announce(RSPlugin plugin, ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            SendMessage packet =
                    new SendMessage(
                            target, ComponentFormatter.mini(plugin.getPrefix()) + minimessage);
            framework().getProtoWeaver().sendPacket(packet);
        } else PlayerChat.of(plugin, player).announce(minimessage);
    }

    public static void announce(RSPlugin plugin, ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            String message = ComponentFormatter.mini(plugin.getPrefix().append(component));
            framework().getProtoWeaver().sendPacket(new SendMessage(target, message));
        } else PlayerChat.of(plugin, player).announce(component);
    }

    public static void send(ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            framework().getProtoWeaver().sendPacket(new SendMessage(target, minimessage));
        } else PlayerChat.of(framework().getPlugin(), player).send(minimessage);
    }

    public static void send(ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            framework()
                    .getProtoWeaver()
                    .sendPacket(new SendMessage(target, ComponentFormatter.mini(component)));
        } else PlayerChat.of(framework().getPlugin(), player).send(component);
    }

    public void setReceiver(CommandSender sender) {
        this.receiver = plugin.getAdventure().sender(sender);
    }

    public void setReceiver(Player player) {
        this.receiver = plugin.getAdventure().player(player);
    }

    public void send(String minimessage) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        this.send(ComponentFormatter.mini(minimessage));
    }

    public void send(Component component) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendMessage(component);
    }

    public void announce(String minimessage) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        this.announce(ComponentFormatter.mini(minimessage));
    }

    public void announce(Component component) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        Component prefix = plugin.getPrefix();
        receiver.sendMessage(prefix.append(component));
    }

    public void send(CommandSender sender, String minimessage) {
        this.send(sender, ComponentFormatter.mini(minimessage));
    }

    public void send(CommandSender sender, Component component) {
        plugin.getAdventure().sender(sender).sendMessage(component);
    }

    public void announce(CommandSender sender, String minimessage) {
        this.announce(sender, ComponentFormatter.mini(minimessage));
    }

    public void announce(CommandSender sender, Component component) {
        Component prefix = plugin.getPrefix();
        plugin.getAdventure().sender(sender).sendMessage(prefix.append(component));
    }

    public void send(Player player, String minimessage) {
        this.send(player, ComponentFormatter.mini(minimessage));
    }

    public void send(Player player, Component component) {
        plugin.getAdventure().player(player).sendMessage(component);
    }

    public void announce(Player player, String minimessage) {
        this.announce(player, ComponentFormatter.mini(minimessage));
    }

    public void announce(Player player, Component component) {
        Component prefix = plugin.getPrefix();
        plugin.getAdventure().player(player).sendMessage(prefix.append(component));
    }

    public void send(Audience audience, String minimessage) {
        this.send(audience, ComponentFormatter.mini(minimessage));
    }

    public void send(Audience audience, Component component) {
        audience.sendMessage(component);
    }

    public void announce(Audience audience, String minimessage) {
        this.announce(audience, ComponentFormatter.mini(minimessage));
    }

    public void announce(Audience audience, Component component) {
        Component prefix = plugin.getPrefix();
        audience.sendMessage(prefix.append(component));
    }
}
