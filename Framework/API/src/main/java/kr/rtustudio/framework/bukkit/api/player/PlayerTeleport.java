package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyLocation;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.broker.protoweaver.bukkit.api.ProtoWeaver;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 플레이어 텔레포트를 처리하는 유틸리티 클래스입니다.
 *
 * <p>로컬 서버 내 텔레포트는 Bukkit/Paper API를 사용하고, 타 서버 텔레포트는 ProtoWeaver를 통해 프록시에 요청합니다.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerTeleport {

    private static final CompletableFuture<Boolean> FALSE =
            CompletableFuture.completedFuture(false);

    static Framework framework;
    private final Player player;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * 플레이어를 대상으로 텔레포트 유틸리티를 생성한다.
     *
     * @param player 텔레포트할 플레이어
     * @return 텔레포트 유틸리티
     */
    public static PlayerTeleport of(@NotNull Player player) {
        return new PlayerTeleport(player);
    }

    /** 현재 서버 이름을 반환한다. */
    private String server() {
        return framework().getBroker(ProtoWeaver.class).getServer();
    }

    /** ProtoWeaver 브로커를 반환한다. */
    private ProtoWeaver protoWeaver() {
        return framework().getBroker(ProtoWeaver.class);
    }

    /**
     * 프록시 위치로 텔레포트한다.
     *
     * <p>대상 서버가 현재 서버면 로컬 텔레포트, 다른 서버면 ProtoWeaver를 통해 요청한다.
     *
     * @param location 대상 프록시 위치
     * @return 텔레포트 성공 여부
     */
    public CompletableFuture<Boolean> teleport(@NotNull ProxyLocation location) {
        if (server().equalsIgnoreCase(location.server())) {
            World world = Bukkit.getWorld(location.world());
            if (world == null) return FALSE;
            return teleport(new Location(world, location.x(), location.y(), location.z()));
        }
        ProtoWeaver pw = protoWeaver();
        if (!pw.isConnected()) return FALSE;
        ProxyPlayer pp = PlayerList.getPlayer(player.getUniqueId());
        LocationTeleport packet = new LocationTeleport(pp, location);
        return CompletableFuture.supplyAsync(() -> pw.publish(packet));
    }

    /**
     * 프록시 플레이어에게 텔레포트한다.
     *
     * <p>대상이 현재 서버에 있으면 로컬 텔레포트, 다른 서버면 ProtoWeaver를 통해 요청한다.
     *
     * @param target 대상 프록시 플레이어
     * @return 텔레포트 성공 여부
     */
    public CompletableFuture<Boolean> teleport(@NotNull ProxyPlayer target) {
        if (server().equalsIgnoreCase(target.server())) {
            Player targetPlayer = Bukkit.getPlayer(target.uniqueId());
            if (targetPlayer == null) return FALSE;
            return teleport(targetPlayer.getLocation());
        }
        ProtoWeaver pw = protoWeaver();
        if (!pw.isConnected()) return FALSE;
        ProxyPlayer pp = PlayerList.getPlayer(player.getUniqueId());
        var packet =
                new kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.PlayerTeleport(
                        pp, target);
        return CompletableFuture.supplyAsync(() -> pw.publish(packet));
    }

    /**
     * Bukkit {@link Location}으로 텔레포트한다.
     *
     * <p>Paper 환경에서는 비동기 텔레포트, Spigot에서는 동기 텔레포트를 사용한다.
     *
     * @param location 대상 위치
     * @return 텔레포트 성공 여부
     */
    public CompletableFuture<Boolean> teleport(@NotNull Location location) {
        if (player == null || !player.isOnline()) return FALSE;
        if (MinecraftVersion.isPaper()) return player.teleportAsync(location);
        try {
            player.teleport(location);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return FALSE;
        }
    }

    /**
     * 대상 플레이어의 현재 위치로 텔레포트한다.
     *
     * @param target 대상 플레이어
     * @return 텔레포트 성공 여부
     */
    public CompletableFuture<Boolean> teleport(@NotNull Player target) {
        return teleport(target.getLocation());
    }
}
