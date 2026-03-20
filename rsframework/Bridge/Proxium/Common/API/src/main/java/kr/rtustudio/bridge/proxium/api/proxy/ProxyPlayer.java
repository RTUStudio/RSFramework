package kr.rtustudio.bridge.proxium.api.proxy;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.ProxiumPipeline;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerActionBar;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerTitle;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.util.Locale;
import java.util.UUID;

/**
 * 프록시 네트워크에 접속해 있는 플레이어 정보를 나타내는 레코드.
 *
 * <p>상태가 업데이트될 수 있도록 클래스로 구현되었으며, 외부에서 임의로 수정할 수 없습니다.
 */
public class ProxyPlayer {

    private final ProxiumPipeline proxium;
    private final UUID uniqueId;
    private final String name;
    private Locale locale;
    private String server;

    /** ProxyPlayer 객체를 생성한다. 내부 시스템 전용. */
    public ProxyPlayer(
            ProxiumPipeline proxium, UUID uniqueId, String name, Locale locale, String server) {
        this.proxium = proxium;
        this.uniqueId = uniqueId;
        this.name = name;
        this.locale = locale;
        this.server = server;
    }

    public ProxiumNode getNode() {
        return proxium.getServer(server);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getServer() {
        return server;
    }

    /** 로케일 정보를 내부적으로 갱신한다. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /** 서버 정보를 내부적으로 갱신한다. */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * 대상 플레이어에게 텔레포트한다.
     *
     * <p>같은 서버에 있으면 네이티브 텔레포트, 다른 서버면 프록시를 통해 크로스 서버 이동.
     *
     * @param target 대상 플레이어
     * @return 패킷 전송 성공 여부
     */
    public boolean teleport(ProxyPlayer target) {
        return proxium.send(new TeleportRequest(this, target));
    }

    /**
     * 대상 위치로 텔레포트한다.
     *
     * @param location 대상 크로스 서버 위치
     * @return 패킷 전송 성공 여부
     */
    public boolean teleport(ProxyLocation location) {
        return proxium.send(new TeleportRequest(this, location));
    }

    /** Minimessage 형식의 문자열 메시지를 전송한다. */
    public void sendMessage(String minimessage) {
        sendMessage(MiniMessage.miniMessage().deserialize(minimessage));
    }

    /** 텍스트 컴포넌트 형식의 메시지를 전송한다. */
    public void sendMessage(Component component) {
        proxium.publish(
                BridgeChannel.AUDIENCE,
                new PlayerMessage(this, MiniMessage.miniMessage().serialize(component)));
    }

    /** Minimessage 형식의 액션바 메시지를 전송한다. */
    public void sendActionBar(String minimessage) {
        sendActionBar(MiniMessage.miniMessage().deserialize(minimessage));
    }

    /** 텍스트 컴포넌트 형식의 액션바 메시지를 전송한다. */
    public void sendActionBar(Component component) {
        proxium.publish(
                BridgeChannel.AUDIENCE,
                new PlayerActionBar(this, MiniMessage.miniMessage().serialize(component)));
    }

    /** 타이틀을 전송한다. */
    public void showTitle(Title title) {
        int fadeIn = 10, stay = 70, fadeOut = 20; // Default ticks
        Title.Times times = title.times();
        if (times != null) {
            fadeIn = (int) (times.fadeIn().toMillis() / 50L);
            stay = (int) (times.stay().toMillis() / 50L);
            fadeOut = (int) (times.fadeOut().toMillis() / 50L);
        }

        proxium.publish(
                BridgeChannel.AUDIENCE,
                new PlayerTitle(
                        this,
                        MiniMessage.miniMessage().serialize(title.title()),
                        MiniMessage.miniMessage().serialize(title.subtitle()),
                        fadeIn,
                        stay,
                        fadeOut));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyPlayer that)) return false;
        return uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
