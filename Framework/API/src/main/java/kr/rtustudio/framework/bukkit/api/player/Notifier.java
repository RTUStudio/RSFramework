package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.bridge.protoweaver.api.protocol.internal.Broadcast;
import kr.rtustudio.bridge.protoweaver.api.protocol.internal.SendMessage;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.protoweaver.bukkit.api.ProtoWeaver;
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

/**
 * 플레이어 및 콘솔에게 메시지를 전송하는 유틸리티 클래스입니다.
 *
 * <p>접두사 포함 안내({@code announce}), 접두사 없는 전송({@code send}), 타이틀, 액션바, 보스바 등 다양한 전송 방식을 지원합니다.
 * ProtoWeaver를 통한 크로스 서버 브로드캐스트도 지원합니다.
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
     * 수신자 없이 Notifier를 생성한다. 사용 전 {@link #setReceiver}로 수신자를 설정해야 한다.
     *
     * @param plugin 소유 플러그인
     * @return 새 Notifier 인스턴스
     */
    public static Notifier of(RSPlugin plugin) {
        return new Notifier(plugin, null);
    }

    /**
     * {@link CommandSender}를 수신자로 Notifier를 생성한다.
     *
     * @param plugin 소유 플러그인
     * @param sender 메시지 수신자
     * @return 새 Notifier 인스턴스
     */
    public static Notifier of(RSPlugin plugin, CommandSender sender) {
        return new Notifier(plugin, plugin.getAdventure().sender(sender));
    }

    /**
     * {@link Player}를 수신자로 Notifier를 생성한다.
     *
     * @param plugin 소유 플러그인
     * @param player 메시지 수신 플레이어
     * @return 새 Notifier 인스턴스
     */
    public static Notifier of(RSPlugin plugin, Player player) {
        return new Notifier(plugin, plugin.getAdventure().player(player));
    }

    /**
     * {@link Audience}를 수신자로 Notifier를 생성한다.
     *
     * @param plugin 소유 플러그인
     * @param audience 메시지 수신 대상
     * @return 새 Notifier 인스턴스
     */
    public static Notifier of(RSPlugin plugin, Audience audience) {
        return new Notifier(plugin, audience);
    }

    /**
     * 현재 서버의 모든 플레이어에게 메시지를 브로드캐스트한다.
     *
     * @param minimessage MiniMessage 형식 문자열
     */
    public static void broadcast(String minimessage) {
        broadcast(ComponentFormatter.mini(minimessage));
    }

    /**
     * 현재 서버의 모든 플레이어에게 컴포넌트를 브로드캐스트한다.
     *
     * @param component 전송할 컴포넌트
     */
    public static void broadcast(Component component) {
        framework().getPlugin().getAdventure().all().sendMessage(component);
    }

    /**
     * 필터 조건을 만족하는 플레이어에게만 메시지를 브로드캐스트한다.
     *
     * @param filter 수신자 필터
     * @param minimessage MiniMessage 형식 문자열
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
     * ProtoWeaver를 통해 모든 서버에 메시지를 브로드캐스트한다.
     *
     * <p>ProtoWeaver 연결이 없으면 현재 서버에만 브로드캐스트한다.
     *
     * @param minimessage MiniMessage 형식 문자열
     */
    public static void broadcastAll(String minimessage) {
        ProtoWeaver pw = framework().getBridge(ProtoWeaver.class);
        if (pw.isConnected()) {
            if (pw.send(new Broadcast(minimessage))) return;
        }
        broadcast(minimessage);
    }

    /**
     * ProtoWeaver를 통해 모든 서버에 컴포넌트를 브로드캐스트한다.
     *
     * <p>ProtoWeaver 연결이 없으면 현재 서버에만 브로드캐스트한다.
     *
     * @param component 전송할 컴포넌트
     */
    public static void broadcastAll(Component component) {
        ProtoWeaver pw = framework().getBridge(ProtoWeaver.class);
        if (pw.isConnected()) {
            if (pw.send(new Broadcast(ComponentFormatter.mini(component)))) return;
        }
        broadcast(component);
    }

    /**
     * 특정 프록시 플레이어에게 접두사 포함 메시지를 전송한다.
     *
     * <p>대상이 현재 서버에 없으면 ProtoWeaver를 통해 전송한다.
     *
     * @param plugin 소유 플러그인
     * @param target 대상 프록시 플레이어
     * @param minimessage MiniMessage 형식 문자열
     */
    public static void announce(RSPlugin plugin, ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            SendMessage packet =
                    new SendMessage(
                            target, ComponentFormatter.mini(plugin.getPrefix()) + minimessage);
            framework().getBridge(ProtoWeaver.class).send(packet);
        } else Notifier.of(plugin, player).announce(minimessage);
    }

    public static void announce(RSPlugin plugin, ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            String message = ComponentFormatter.mini(plugin.getPrefix().append(component));
            framework().getBridge(ProtoWeaver.class).send(new SendMessage(target, message));
        } else Notifier.of(plugin, player).announce(component);
    }

    public static void send(ProxyPlayer target, String minimessage) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            framework().getBridge(ProtoWeaver.class).send(new SendMessage(target, minimessage));
        } else Notifier.of(framework().getPlugin(), player).send(minimessage);
    }

    public static void send(ProxyPlayer target, Component component) {
        Player player = Bukkit.getPlayer(target.uniqueId());
        if (player == null) {
            framework()
                    .getBridge(ProtoWeaver.class)
                    .send(new SendMessage(target, ComponentFormatter.mini(component)));
        } else Notifier.of(framework().getPlugin(), player).send(component);
    }

    /**
     * 메시지 수신자를 {@link CommandSender}로 설정한다.
     *
     * @param sender 새 수신자
     */
    public void setReceiver(CommandSender sender) {
        this.receiver = plugin.getAdventure().sender(sender);
    }

    /**
     * 메시지 수신자를 {@link Player}로 설정한다.
     *
     * @param player 새 수신자
     */
    public void setReceiver(Player player) {
        this.receiver = plugin.getAdventure().player(player);
    }

    /**
     * 수신자에게 접두사 없이 MiniMessage 형식 메시지를 전송한다.
     *
     * @param minimessage MiniMessage 형식 문자열
     */
    public void send(String minimessage) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        this.send(ComponentFormatter.mini(minimessage));
    }

    /**
     * 수신자에게 접두사 없이 컴포넌트를 전송한다.
     *
     * @param component 전송할 컴포넌트
     */
    public void send(Component component) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendMessage(component);
    }

    /**
     * 수신자에게 플러그인 접두사를 포함하여 MiniMessage 형식 메시지를 전송한다.
     *
     * @param minimessage MiniMessage 형식 문자열
     */
    public void announce(String minimessage) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        this.announce(ComponentFormatter.mini(minimessage));
    }

    /**
     * 수신자에게 플러그인 접두사를 포함하여 컴포넌트를 전송한다.
     *
     * @param component 전송할 컴포넌트
     */
    public void announce(Component component) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        Component prefix = plugin.getPrefix();
        receiver.sendMessage(prefix.append(component));
    }

    /**
     * 지정한 {@link CommandSender}에게 접두사 없이 메시지를 전송한다.
     *
     * @param sender 수신자
     * @param minimessage MiniMessage 형식 문자열
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
     * 수신자에게 타이틀을 표시한다.
     *
     * @param title 타이틀 컴포넌트
     * @param subtitle 서브타이틀 컴포넌트
     */
    public void title(Component title, Component subtitle) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.showTitle(Title.title(title, subtitle));
    }

    /**
     * 수신자에게 타이밍을 지정하여 타이틀을 표시한다.
     *
     * @param title 타이틀 컴포넌트
     * @param subtitle 서브타이틀 컴포넌트
     * @param times 페이드인/유지/페이드아웃 타이밍
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
     * 수신자에게 액션바 메시지를 표시한다.
     *
     * @param message 표시할 컴포넌트
     */
    public void actionbar(Component message) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.sendActionBar(message);
    }

    public void actionbar(String minimessage) {
        actionbar(ComponentFormatter.mini(minimessage));
    }

    // ── Boss Bar ──

    /**
     * 수신자에게 보스바를 표시한다.
     *
     * @param name 보스바 이름 컴포넌트
     * @param progress 진행률 (0.0 ~ 1.0)
     * @param color 보스바 색상
     * @param overlay 보스바 오버레이 스타일
     * @return 생성된 {@link BossBar} 인스턴스
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
     * 수신자에게 표시된 보스바를 숨긴다.
     *
     * @param bar 숨길 보스바
     */
    public void hideBossbar(BossBar bar) {
        if (this.receiver == null) throw new UnsupportedOperationException("Audience is null");
        receiver.hideBossBar(bar);
    }
}
