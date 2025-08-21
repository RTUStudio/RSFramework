package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtuserver.protoweaver.api.proxy.ProxyLocation;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtuserver.protoweaver.bukkit.api.BukkitProtoWeaver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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
        return framework().getProtoWeaver().getServer();
    }

    private BukkitProtoWeaver protoWeaver() {
        return framework().getProtoWeaver();
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
                return CompletableFuture.supplyAsync(() -> protoWeaver().sendPacket(packet));
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
                kr.rtuserver.protoweaver.api.proxy.request.teleport.PlayerTeleport packet = new kr.rtuserver.protoweaver.api.proxy.request.teleport.PlayerTeleport(pp, target);
                return CompletableFuture.supplyAsync(() -> protoWeaver().sendPacket(packet));
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> teleport(Location location) {
        Player player = getPlayer();
        if (player == null) return CompletableFuture.completedFuture(false);
        if (MinecraftVersion.isPaper()) return player.teleportAsync(location);
        else {
            try {
                player.teleport(location);
                return CompletableFuture.completedFuture(true);
            } catch (Exception e) {
                return CompletableFuture.completedFuture(false);
            }
        }
    }

    public CompletableFuture<Boolean> teleport(Player target) {
        Player player = getPlayer();
        if (player == null) return CompletableFuture.completedFuture(false);
        if (MinecraftVersion.isPaper()) return player.teleportAsync(target.getLocation());
        else {
            try {
                player.teleport(target.getLocation());
                return CompletableFuture.completedFuture(true);
            } catch (Exception e) {
                return CompletableFuture.completedFuture(false);
            }
        }
    }
}