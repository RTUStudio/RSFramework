package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.protoweaver.api.protocol.internal.BroadcastChat;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class PlayerChat {

    private final RSPlugin plugin;
    @Getter
    private Audience receiver;

    PlayerChat(RSPlugin plugin, Audience audience) {
        this.plugin = plugin;
        this.receiver = audience;
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

    public void broadcast(String minimessage) {
        this.broadcast(ComponentFormatter.mini(minimessage));
    }

    public void broadcast(Component component) {
        plugin.getAdventure().all().sendMessage(component);
    }

    public void broadcast(Predicate<CommandSender> filter, Component component) {
        this.broadcast(filter, ComponentFormatter.mini(component));
    }

    public void broadcast(Predicate<CommandSender> filter, String minimessage) {
        plugin.getAdventure().filter(filter).sendMessage(ComponentFormatter.mini(minimessage));
    }

    public void broadcastAll(String minimessage) {
        if (!plugin.getFramework().getProtoWeaver().sendPacket(new BroadcastChat(minimessage)))
            broadcast(minimessage);
    }

    public void broadcastAll(Component component) {
        if (!plugin.getFramework().getProtoWeaver().sendPacket(new BroadcastChat(ComponentFormatter.mini(component))))
            broadcast(component);
    }

}
