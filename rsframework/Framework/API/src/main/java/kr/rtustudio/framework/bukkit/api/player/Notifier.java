package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.protocol.internal.BroadcastMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerMessage;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Supports prefix-included announcements ({@code announce}), prefix-less sends ({@code send}),
 * titles, action bars, boss bars, and cross-server broadcasting via Proxium.
 *
 * <p>플레이어 및 콘솔에게 메시지를 전송하는 유틸리티 클래스. 접두사 포함 안내, 접두사 없는 전송, 타이틀, 액션바, 보스바 등 다양한 전송 방식을 지원한다.
 * Proxium를 통한 크로스 서버 브로드캐스트도 지원한다.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Notifier {

    private final RSPlugin plugin;
    @Getter private Audience receiver;

    Notifier(RSPlugin plugin, Audience audience) {
        this.plugin = plugin;
        this.receiver = audience;
    }

    static Framework framework() {
        return LightDI.getBean(Framework.class);
    }

    /**
     * Creates a Notifier without a receiver. Set receiver via {@link #setReceiver} before use.
     *
     * <p>수신자 없이 Notifier를 생성한다. 사용 전 {@link #setReceiver}로 수신자를 설정해야 한다.
     *
     * @param plugin owning plugin
     * @return new Notifier instance
     */
    public static Notifier of(RSPlugin plugin) {
        return new Notifier(plugin, null);
    }

    /**
     * Creates a Notifier with a {@link CommandSender} as receiver.
     *
     * <p>{@link CommandSender}를 수신자로 Notifier를 생성한다.
     *
     * @param plugin owning plugin
     * @param sender message receiver
     * @return new Notifier instance
     */
    public static Notifier of(RSPlugin plugin, CommandSender sender) {
        return new Notifier(plugin, plugin.getAdventure().sender(sender));
    }

    /**
     * Creates a Notifier with a {@link Player} as receiver.
     *
     * <p>{@link Player}를 수신자로 Notifier를 생성한다.
     *
     * @param plugin owning plugin
     * @param player receiving player
     * @return new Notifier instance
     */
    public static Notifier of(RSPlugin plugin, Player player) {
        return new Notifier(plugin, plugin.getAdventure().player(player));
    }

    /**
     * Creates a Notifier with an {@link Audience} as receiver.
     *
     * <p>{@link Audience}를 수신자로 Notifier를 생성한다.
     *
     * @param plugin owning plugin
     * @param audience message receiver
     * @return new Notifier instance
     */
    public static Notifier of(RSPlugin plugin, Audience audience) {
        return new Notifier(plugin, audience);
    }

    /**
     * Broadcasts a message to all players on the current server.
     *
     * <p>현재 서버의 모든 플레이어에게 메시지를 브로드캐스트한다.
     *
     * @param minimessage MiniMessage format string
     */
    public static void broadcast(String minimessage) {
        broadcast(ComponentFormatter.mini(minimessage));
    }

    /**
     * Broadcasts a component to all players on the current server.
     *
     * <p>현재 서버의 모든 플레이어에게 컴포넌트를 브로드캐스트한다.
     *
     * @param component component to send
     */
    public static void broadcast(Component component) {
        framework().getPlugin().getAdventure().all().sendMessage(component);
    }

    /**
     * Broadcasts a message only to players matching the filter.
     *
     * <p>필터 조건을 만족하는 플레이어에게만 메시지를 브로드캐스트한다.
     *
     * @param filter receiver filter
     * @param minimessage MiniMessage format string
     */
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

    /**
     * Broadcasts a message to all servers via Proxium. Falls back to local broadcast if Proxium is
     * not connected.
     *
     * <p>Proxium를 통해 모든 서버에 메시지를 브로드캐스트한다. Proxium 연결이 없으면 현재 서버에만 브로드캐스트한다.
     *
     * @param minimessage MiniMessage format string
     */
    public static void broadcastAll(String minimessage) {
        Proxium pw = framework().getBridge(Proxium.class);
        if (pw.isConnected()) {
            pw.publish(BridgeChannel.AUDIENCE, new BroadcastMessage(minimessage));
            return;
        }
        broadcast(minimessage);
    }

    /**
     * Broadcasts a component to all servers via Proxium. Falls back to local broadcast if Proxium
     * is not connected.
     *
     * <p>Proxium를 통해 모든 서버에 컴포넌트를 브로드캐스트한다. Proxium 연결이 없으면 현재 서버에만 브로드캐스트한다.
     *
     * @param component component to send
     */
    public static void broadcastAll(Component component) {
        Proxium pw = framework().getBridge(Proxium.class);
        if (pw.isConnected()) {
            pw.publish(
                    BridgeChannel.AUDIENCE,
                    new BroadcastMessage(MiniMessage.miniMessage().serialize(component)));
            return;
        }
        broadcast(component);
    }

    /**
     * Sends a prefixed message to a specific proxy player. If the target is not on the current
     * server, sends via Proxium.
     *
     * <p>특정 프록시 플레이어에게 접두사 포함 메시지를 전송한다. 대상이 현재 서버에 없으면 Proxium를 통해 전송한다.
     *
     * @param plugin owning plugin
     * @param target target proxy player
     * @param minimessage MiniMessage format string
     */
    public static void announce(RSPlugin plugin, ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player == null) {
            PlayerMessage packet =
                    new PlayerMessage(
                            target,
                            MiniMessage.miniMessage()
                                    .serialize(
                                            plugin.getPrefix()
                                                    .append(ComponentFormatter.mini(minimessage))));
            framework().getBridge(Proxium.class).publish(BridgeChannel.AUDIENCE, packet);
        } else Notifier.of(plugin, player).announce(minimessage);
    }

    public static void announce(RSPlugin plugin, ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player == null) {
            Component messageComponent = plugin.getPrefix().append(component);
            framework()
                    .getBridge(Proxium.class)
                    .publish(
                            BridgeChannel.AUDIENCE,
                            new PlayerMessage(
                                    target, MiniMessage.miniMessage().serialize(messageComponent)));
        } else Notifier.of(plugin, player).announce(component);
    }

    public static void send(ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player == null) {
            framework()
                    .getBridge(Proxium.class)
                    .publish(BridgeChannel.AUDIENCE, new PlayerMessage(target, minimessage));
        } else Notifier.of(framework().getPlugin(), player).send(minimessage);
    }

    public static void send(ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player == null) {
            framework()
                    .getBridge(Proxium.class)
                    .publish(
                            BridgeChannel.AUDIENCE,
                            new PlayerMessage(
                                    target, MiniMessage.miniMessage().serialize(component)));
        } else Notifier.of(framework().getPlugin(), player).send(component);
    }

    /**
     * Sets the message receiver to a {@link CommandSender}.
     *
     * <p>메시지 수신자를 {@link CommandSender}로 설정한다.
     *
     * @param sender new receiver
     */
    public void setReceiver(CommandSender sender) {
        this.receiver = plugin.getAdventure().sender(sender);
    }

    /**
     * Sets the message receiver to a {@link Player}.
     *
     * <p>메시지 수신자를 {@link Player}로 설정한다.
     *
     * @param player new receiver
     */
    public void setReceiver(Player player) {
        this.receiver = plugin.getAdventure().player(player);
    }

    /**
     * Sends a MiniMessage-formatted message without prefix to the receiver.
     *
     * <p>수신자에게 접두사 없이 MiniMessage 형식 메시지를 전송한다.
     *
     * @param minimessage MiniMessage format string
     */
    public void send(String minimessage) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        this.send(ComponentFormatter.mini(minimessage));
    }

    /**
     * Sends a component without prefix to the receiver.
     *
     * <p>수신자에게 접두사 없이 컴포넌트를 전송한다.
     *
     * @param component component to send
     */
    public void send(Component component) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendMessage(component);
    }

    /**
     * Sends a MiniMessage-formatted message with plugin prefix to the receiver.
     *
     * <p>수신자에게 플러그인 접두사를 포함하여 MiniMessage 형식 메시지를 전송한다.
     *
     * @param minimessage MiniMessage format string
     */
    public void announce(String minimessage) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        this.announce(ComponentFormatter.mini(minimessage));
    }

    /**
     * Sends a component with plugin prefix to the receiver.
     *
     * <p>수신자에게 플러그인 접두사를 포함하여 컴포넌트를 전송한다.
     *
     * @param component component to send
     */
    public void announce(Component component) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        Component prefix = plugin.getPrefix();
        receiver.sendMessage(prefix.append(component));
    }

    /**
     * Sends a message without prefix to the specified {@link CommandSender}.
     *
     * <p>지정한 {@link CommandSender}에게 접두사 없이 메시지를 전송한다.
     *
     * @param sender receiver
     * @param minimessage MiniMessage format string
     */
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

    /**
     * Shows a title to the receiver.
     *
     * <p>수신자에게 타이틀을 표시한다.
     *
     * @param title title component
     * @param subtitle subtitle component
     */
    public void title(Component title, Component subtitle) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.showTitle(Title.title(title, subtitle));
    }

    /**
     * Shows a title with timing to the receiver.
     *
     * <p>수신자에게 타이밍을 지정하여 타이틀을 표시한다.
     *
     * @param title title component
     * @param subtitle subtitle component
     * @param times fade-in/stay/fade-out timing
     */
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

    /**
     * Shows an action bar message to the receiver.
     *
     * <p>수신자에게 액션바 메시지를 표시한다.
     *
     * @param message component to display
     */
    public void actionbar(Component message) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendActionBar(message);
    }

    public void actionbar(String minimessage) {
        actionbar(ComponentFormatter.mini(minimessage));
    }

    public void sendActionBar(Component message) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendActionBar(message);
    }

    public void showTitle(net.kyori.adventure.title.Title title) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.showTitle(title);
    }

    // ── Boss Bar ──

    /**
     * Shows a boss bar to the receiver.
     *
     * <p>수신자에게 보스바를 표시한다.
     *
     * @param name boss bar name component
     * @param progress progress (0.0 ~ 1.0)
     * @param color boss bar color
     * @param overlay boss bar overlay style
     * @return created {@link BossBar} instance
     */
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

    /**
     * Hides a boss bar from the receiver.
     *
     * <p>수신자에게 표시된 보스바를 숨긴다.
     *
     * @param bar boss bar to hide
     */
    public void hideBossbar(BossBar bar) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.hideBossBar(bar);
    }
}
