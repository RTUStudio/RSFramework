package kr.rtuserver.framework.bukkit.api.utility.player;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import kr.rtuserver.protoweaver.api.protocol.internal.BroadcastChat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class PlayerChat {

    private final RSPlugin plugin;

    private PlayerChat(RSPlugin plugin) {
        this.plugin = plugin;
    }

    public static PlayerChat of(RSPlugin plugin) {
        return new PlayerChat(plugin);
    }

    public void send(CommandSender sender, String minimessage) {
        Audience audience = plugin.getAdventure().sender(sender);
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    public void send(Player player, String minimessage) {
        Audience audience = plugin.getAdventure().player(player);
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    public void send(Audience audience, String minimessage) {
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    public void send(CommandSender sender, Component component) {
        Audience audience = plugin.getAdventure().sender(sender);
        audience.sendMessage(component);
    }

    public void send(Player player, Component component) {
        Audience audience = plugin.getAdventure().player(player);
        audience.sendMessage(component);
    }

    public void send(Audience audience, Component component) {
        audience.sendMessage(component);
    }

    public void broadcast(Component component) {
        plugin.getAdventure().all().sendMessage(component);
    }

    public void broadcast(Predicate<CommandSender> filter, Component component) {
        plugin.getAdventure().filter(filter).sendMessage(component);
    }

    public void broadcastAll(Component component) {
        if (!plugin.getFramework().getProtoWeaver().sendPacket(new BroadcastChat(ComponentFormatter.mini(component))))
            broadcast(component);
    }

    public void broadcast(String minimessage) {
        plugin.getAdventure().all().sendMessage(ComponentFormatter.mini(minimessage));
    }

    public void broadcast(Predicate<CommandSender> filter, String minimessage) {
        plugin.getAdventure().filter(filter).sendMessage(ComponentFormatter.mini(minimessage));
    }

    public void broadcastAll(String minimessage) {
        if (!plugin.getFramework().getProtoWeaver().sendPacket(new BroadcastChat(minimessage))) broadcast(minimessage);
    }

    public void announce(CommandSender sender, String minimessage) {
        Audience audience = plugin.getAdventure().sender(sender);
        Component prefix = plugin.getConfigurations().getMessage().getPrefix();
        audience.sendMessage(prefix.append(ComponentFormatter.mini(minimessage)));
    }

    public void announce(Player player, String minimessage) {
        Audience audience = plugin.getAdventure().player(player);
        Component prefix = plugin.getConfigurations().getMessage().getPrefix();
        audience.sendMessage(prefix.append(ComponentFormatter.mini(minimessage)));
    }

    public void announce(Audience audience, String minimessage) {
        Component prefix = plugin.getConfigurations().getMessage().getPrefix();
        audience.sendMessage(prefix.append(ComponentFormatter.mini(minimessage)));
    }

    public void announce(CommandSender sender, Component component) {
        Audience audience = plugin.getAdventure().sender(sender);
        Component prefix = plugin.getConfigurations().getMessage().getPrefix();
        audience.sendMessage(prefix.append(component));
    }

    public void announce(Player player, Component component) {
        Audience audience = plugin.getAdventure().player(player);
        Component prefix = plugin.getConfigurations().getMessage().getPrefix();
        audience.sendMessage(prefix.append(component));
    }

    public void announce(Audience audience, Component component) {
        Component prefix = plugin.getConfigurations().getMessage().getPrefix();
        audience.sendMessage(prefix.append(component));
    }

}
