package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtuserver.protoweaver.api.proxy.ProxyLocation;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtuserver.protoweaver.bukkit.api.BukkitProtoWeaver;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
public class RSPlayer extends ProxyPlayer {

    static Framework framework;

    public RSPlayer(UUID uniqueId, String name) {
        super(uniqueId, framework().getProtoWeaver().getServer(), name);
    }

    public RSPlayer(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static ProxyPlayer getPlayer(UUID uniqueId) {
        return getPlayers().stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public static Set<ProxyPlayer> getPlayers() {
        return getPlayers(true);
    }

    public static Set<ProxyPlayer> getPlayers(boolean includeProxy) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        boolean isConnected = protoWeaver.isConnected();
        if (isConnected && includeProxy) {
            return new HashSet<>(protoWeaver.getPlayers().values());
        } else {
            return Bukkit.getOnlinePlayers().stream().map(RSPlayer::new).collect(Collectors.toSet());
        }
    }

    private boolean update() {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        setServer(protoWeaver.getServer());
        if (protoWeaver.isConnected()) {
            Map<UUID, ProxyPlayer> players = protoWeaver.getPlayers();
            return players.containsKey(getUniqueId());
        } else return true;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(getUniqueId());
    }

    public boolean isOnline() {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (protoWeaver.isConnected()) {
            Map<UUID, ProxyPlayer> players = protoWeaver.getPlayers();
            return players.containsKey(getUniqueId());
        } else return true;
    }

    public CompletableFuture<Boolean> teleport(@NotNull ProxyLocation location) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (update()) {
            if (location.server().equalsIgnoreCase(getServer())) {
                World world = Bukkit.getWorld(location.world());
                if (world == null) return CompletableFuture.completedFuture(false);
                return teleport(new Location(world, location.x(), location.y(), location.z(), location.yaw(), location.pitch()));
            }
            if (protoWeaver.isConnected()) {
                return CompletableFuture.supplyAsync(() -> protoWeaver.sendPacket(new TeleportRequest.Location(this, location)));
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> teleport(@NotNull ProxyPlayer player) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (update()) {
            if (player.getServer().equalsIgnoreCase(getServer())) {
                Player target = Bukkit.getPlayer(player.getUniqueId());
                if (target == null) return CompletableFuture.completedFuture(false);
                return teleport(target.getLocation());
            }
            if (protoWeaver.isConnected()) {
                return CompletableFuture.supplyAsync(() -> protoWeaver.sendPacket(new TeleportRequest.Player(this, player)));
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