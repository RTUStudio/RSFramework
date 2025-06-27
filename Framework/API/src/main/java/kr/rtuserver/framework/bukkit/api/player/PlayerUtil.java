package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtuserver.protoweaver.api.impl.bukkit.BukkitProtoWeaver;
import kr.rtuserver.protoweaver.api.proxy.ProxyLocation;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerUtil {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static ProxyPlayer getPlayer(UUID uuid) {
        return getPlayers().stream().filter(player -> player.uuid().equals(uuid)).findFirst().orElse(null);
    }

    public static Set<ProxyPlayer> getPlayers() {
        return getPlayers(true);
    }

    public static Set<ProxyPlayer> getPlayers(boolean includeProxy) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        boolean isConnected = protoWeaver.isConnected();
        return isConnected && includeProxy ? protoWeaver.getPlayers() : Bukkit.getOnlinePlayers().stream().map(player -> new ProxyPlayer(player.getUniqueId(), player.getName())).collect(Collectors.toSet());
    }

    public static CompletableFuture<Boolean> teleport(ProxyPlayer player, ProxyLocation location) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (protoWeaver.isConnected() || (location.server() != null && !location.server().isEmpty())) {
            return CompletableFuture.supplyAsync(() -> protoWeaver.sendPacket(new TeleportRequest(player, location)));
        } else {
            Player target = Bukkit.getPlayer(player.uuid());
            if (target == null) return CompletableFuture.completedFuture(false);
            World world = Bukkit.getWorld(location.world());
            if (world == null) return CompletableFuture.completedFuture(false);
            Location loc = new Location(world, location.x(), location.y(), location.z(), location.yaw(), location.pitch());
            return teleport(target, loc);
        }
    }

    public static CompletableFuture<Boolean> teleport(Player player, Location location) {
        if (MinecraftVersion.isPaper()) return player.teleportAsync(location);
        else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    player.teleport(location);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
        }
    }

}