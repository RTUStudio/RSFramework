package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyLocation;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.broker.protoweaver.bukkit.api.ProtoWeaver;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerTeleport {

    static Framework framework;
    private final Player player;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static PlayerTeleport of(Player player) {
        return new PlayerTeleport(player);
    }

    private String server() {
        return framework().getBroker(ProtoWeaver.class).getServer();
    }

    private ProtoWeaver protoWeaver() {
        return framework().getBroker(ProtoWeaver.class);
    }

    public CompletableFuture<Boolean> teleport(@NotNull ProxyLocation location) {
        if (server().equalsIgnoreCase(location.server())) {
            World world = Bukkit.getWorld(location.world());
            if (world == null) return CompletableFuture.completedFuture(false);
            Location bukkitLocation = new Location(world, location.x(), location.y(), location.z());
            return teleport(bukkitLocation);
        } else {
            if (protoWeaver().isConnected()) {
                ProxyPlayer pp = PlayerList.getPlayer(player.getUniqueId());
                LocationTeleport packet = new LocationTeleport(pp, location);
                return CompletableFuture.supplyAsync(() -> protoWeaver().publish(packet));
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> teleport(@NotNull ProxyPlayer target) {
        if (server().equalsIgnoreCase(target.server())) {
            Player targetPlayer = Bukkit.getPlayer(target.uniqueId());
            if (targetPlayer == null) return CompletableFuture.completedFuture(false);
            return teleport(targetPlayer.getLocation());
        } else {
            if (protoWeaver().isConnected()) {
                ProxyPlayer pp = PlayerList.getPlayer(player.getUniqueId());
                kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.PlayerTeleport packet =
                        new kr.rtustudio.broker.protoweaver.api.proxy.request.teleport
                                .PlayerTeleport(pp, target);
                return CompletableFuture.supplyAsync(() -> protoWeaver().publish(packet));
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> teleport(Location location) {
        Player player = getPlayer();
        if (player == null) return CompletableFuture.completedFuture(false);
        if (MinecraftVersion.isPaper()) return player.teleportAsync(location);
        try {
            player.teleport(location);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    public CompletableFuture<Boolean> teleport(Player target) {
        return teleport(target.getLocation());
    }
}
