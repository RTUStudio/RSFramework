package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.broker.protoweaver.api.protocol.internal.Broadcast;
import kr.rtustudio.broker.protoweaver.api.protocol.internal.SendMessage;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.broker.protoweaver.bukkit.api.ProtoWeaver;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerAudience {

    static Framework framework;
    private final RSPlugin plugin;
    @Getter private Audience receiver;

    PlayerAudience(RSPlugin plugin, Audience audience) {
        this.plugin = plugin;
        this.receiver = audience;
    }

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static PlayerAudience of(RSPlugin plugin) {
        return new PlayerAudience(plugin, null);
    }

    public static PlayerAudience of(RSPlugin plugin, CommandSender sender) {
        return new PlayerAudience(plugin, plugin.getAdventure().sender(sender));
    }

    public static PlayerAudience of(RSPlugin plugin, Player player) {
        return new PlayerAudience(plugin, plugin.getAdventure().player(player));
    }

    public static PlayerAudience of(RSPlugin plugin, Audience audience) {
        return new PlayerAudience(plugin, audience);
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
        ProtoWeaver pw = framework().getBroker(ProtoWeaver.class);
        if (pw.isConnected()) {
            if (pw.publish(new Broadcast(minimessage))) return;
        }
        broadcast(minimessage);
    }

    public static void broadcastAll(Component component) {
        ProtoWeaver pw = framework().getBroker(ProtoWeaver.class);
        if (pw.isConnected()) {
            if (pw.publish(new Broadcast(ComponentFormatter.mini(component)))) return;
        }
        broadcast(component);
    }

    public static void announce(RSPlugin plugin, ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            SendMessage packet =
                    new SendMessage(
                            target, ComponentFormatter.mini(plugin.getPrefix()) + minimessage);
            framework().getBroker(ProtoWeaver.class).publish(packet);
        } else PlayerAudience.of(plugin, player).announce(minimessage);
    }

    public static void announce(RSPlugin plugin, ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            String message = ComponentFormatter.mini(plugin.getPrefix().append(component));
            framework().getBroker(ProtoWeaver.class).publish(new SendMessage(target, message));
        } else PlayerAudience.of(plugin, player).announce(component);
    }

    public static void send(ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            framework().getBroker(ProtoWeaver.class).publish(new SendMessage(target, minimessage));
        } else PlayerAudience.of(framework().getPlugin(), player).send(minimessage);
    }

    public static void send(ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            framework()
                    .getBroker(ProtoWeaver.class)
                    .publish(new SendMessage(target, ComponentFormatter.mini(component)));
        } else PlayerAudience.of(framework().getPlugin(), player).send(component);
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

    // ── Title ──

    public void title(Component title, Component subtitle) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.showTitle(Title.title(title, subtitle));
    }

    public void title(Component title, Component subtitle, Title.Times times) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.showTitle(Title.title(title, subtitle, times));
    }

    public void title(String title, String subtitle) {
        title(ComponentFormatter.mini(title), ComponentFormatter.mini(subtitle));
    }

    // ── Subtitle (title empty) ──

    public void subtitle(Component subtitle) {
        title(Component.empty(), subtitle);
    }

    public void subtitle(String subtitle) {
        title(Component.empty(), ComponentFormatter.mini(subtitle));
    }

    // ── Action Bar ──

    public void actionbar(Component message) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendActionBar(message);
    }

    public void actionbar(String minimessage) {
        actionbar(ComponentFormatter.mini(minimessage));
    }

    // ── Boss Bar ──

    public BossBar bossbar(
            Component name, float progress, BossBar.Color color, BossBar.Overlay overlay) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        BossBar bar = BossBar.bossBar(name, progress, color, overlay);
        receiver.showBossBar(bar);
        return bar;
    }

    public BossBar bossbar(
            String minimessage, float progress, BossBar.Color color, BossBar.Overlay overlay) {
        return bossbar(ComponentFormatter.mini(minimessage), progress, color, overlay);
    }

    public void hideBossbar(BossBar bar) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.hideBossBar(bar);
    }
}
