package kr.rtustudio.bridge.proxium.api.proxy;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.ProxiumPipeline;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerActionBar;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerTitle;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * 프록시 네트워크에 접속해 있는 플레이어 정보를 나타내는 클래스.
 *
 * <p>상태가 업데이트될 수 있도록 클래스로 구현되었으며, 외부에서 임의로 수정할 수 없습니다.
 */
@Getter
@EqualsAndHashCode(of = "uniqueId")
public class ProxyPlayer {

    private final transient ProxiumPipeline proxium;
    private final UUID uniqueId;
    private final String name;
    @Nullable protected ProxiumNode node;

    /** ProxyPlayer 객체를 생성한다. 내부 시스템 전용. */
    public ProxyPlayer(
            ProxiumPipeline proxium, UUID uniqueId, String name, @Nullable ProxiumNode node) {
        this.proxium = proxium;
        this.uniqueId = uniqueId;
        this.name = name;
        this.node = node;
    }

    /** 플레이어가 있는 서버 이름을 반환한다. */
    @Nullable
    public String getServer() {
        return node != null ? node.name() : null;
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
        TeleportRequest request = new TeleportRequest(this, target);
        if (isLocalTeleport(request)) {
            proxium.publish(BridgeChannel.INTERNAL, request);
            return true;
        }
        return proxium.send(request);
    }

    /**
     * 대상 위치로 텔레포트한다.
     *
     * @param location 대상 크로스 서버 위치
     * @return 패킷 전송 성공 여부
     */
    public boolean teleport(ProxyLocation location) {
        TeleportRequest request = new TeleportRequest(this, location);
        if (isLocalTeleport(request)) {
            proxium.publish(BridgeChannel.INTERNAL, request);
            return true;
        }
        return proxium.send(request);
    }

    /**
     * 텔레포트 요청이 로컬 서버에서 처리 가능한지 확인한다.
     *
     * <p>플레이어가 현재 서버에 있고, 대상 서버도 현재 서버라면 네트워크를 거치지 않고 로컬에서 처리한다.
     */
    private boolean isLocalTeleport(TeleportRequest request) {
        String localServer = proxium.getName();
        String targetServer = request.server();
        return localServer != null
                && localServer.equals(getServer())
                && localServer.equals(targetServer);
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
}
