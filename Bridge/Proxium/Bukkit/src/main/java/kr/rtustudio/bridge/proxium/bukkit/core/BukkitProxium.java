package kr.rtustudio.bridge.proxium.bukkit.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.Broadcast;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.SendMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ServerName;
import kr.rtustudio.bridge.proxium.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.bridge.proxium.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.bridge.proxium.core.ProxiumServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Slf4j(topic = "Proxium")
@Getter
public class BukkitProxium extends ProxiumServer {

    private final Security security;
    private boolean loaded = false;

    public BukkitProxium(
            String sslFolder,
            BridgeOptions options,
            CompressionType compression,
            int maxPacketSize) {
        super(options);
        this.security = new Security(sslFolder);
        this.security.setup(options.isTls());

        try {
            Protocol.Builder protocol = Protocol.create(BridgeChannel.INTERNAL);
            protocol.setOptions(options);
            protocol.setCompression(compression);
            protocol.setMaxPacketSize(maxPacketSize);

            options.register(
                    BridgeChannel.INTERNAL,
                    BridgeChannel.class,
                    SendMessage.class,
                    Broadcast.class,
                    ServerName.class,
                    PlayerList.class,
                    LocationTeleport.class,
                    PlayerTeleport.class);
            registeredChannels.add(BridgeChannel.INTERNAL);

            registerInternalSubscription();

            if (this.security.isModernProxy()) {
                protocol.setServerAuthHandler(VelocityAuth::new);
            }

            protocol.setServerHandler(() -> new ConnectionHandler(this)).load();
            this.loaded = true;
        } catch (Exception e) {
            log.error("Failed to initialize Proxium", e);
            this.loaded = false;
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    private void registerInternalSubscription() {
        subscribe(
                BridgeChannel.INTERNAL,
                packet -> {
                    if (packet instanceof PlayerList playerList) {
                        this.players.clear();
                        this.players.putAll(playerList.players());
                    } else if (packet instanceof TeleportRequest request) {
                        handleTeleport(request);
                    } else if (packet instanceof ServerName sn) {
                        this.serverName = sn.name();
                    }
                });
    }

    private void handleTeleport(TeleportRequest request) {
        Player player = Bukkit.getPlayer(request.player().uniqueId());
        if (player == null) return;

        Location location = null;
        if (request instanceof LocationTeleport lt) {
            ProxyLocation loc = lt.location();
            World world = Bukkit.getWorld(loc.world());
            if (world != null) {
                location = new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
            }
        } else if (request instanceof PlayerTeleport pt) {
            Player target = Bukkit.getPlayer(pt.target().uniqueId());
            if (target != null) {
                location = target.getLocation();
            }
        }

        if (location == null) return;

        if (security.isPaper()) {
            player.teleportAsync(location);
        } else {
            player.teleport(location);
        }
    }
}
