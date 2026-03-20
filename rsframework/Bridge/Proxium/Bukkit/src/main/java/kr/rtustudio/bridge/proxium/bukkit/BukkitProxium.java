package kr.rtustudio.bridge.proxium.bukkit;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.protocol.CompressionType;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.BroadcastMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerActionBar;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerEvent;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerTitle;
import kr.rtustudio.bridge.proxium.api.protocol.internal.RequestPacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ResponsePacket;
import kr.rtustudio.bridge.proxium.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
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
                    ProxiumNode.class,
                    PlayerList.class,
                    TeleportRequest.class,
                    PlayerEvent.class,
                    RequestPacket.class,
                    ResponsePacket.class);
            options.register(
                    BridgeChannel.AUDIENCE,
                    BridgeChannel.class,
                    PlayerMessage.class,
                    BroadcastMessage.class,
                    PlayerActionBar.class,
                    PlayerTitle.class);
            registeredChannels.add(BridgeChannel.INTERNAL);
            registeredChannels.add(BridgeChannel.AUDIENCE);

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
                        java.util.Set<java.util.UUID> incomingUuids = playerList.players().keySet();
                        // 1. Remove players no longer in the proxy list
                        this.players.keySet().removeIf(uuid -> !incomingUuids.contains(uuid));

                        // 2. Update existing or add new
                        playerList
                                .players()
                                .forEach(
                                        (uuid, pp) -> {
                                            trackServer(pp.getServer());
                                            kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer
                                                    local = this.players.get(uuid);
                                            if (local != null) {
                                                local.setServer(pp.getServer());
                                                local.setLocale(pp.getLocale());
                                            } else {
                                                this.players.put(
                                                        uuid,
                                                        new kr.rtustudio.bridge.proxium.api.proxy
                                                                .ProxyPlayer(
                                                                this,
                                                                pp.getUniqueId(),
                                                                pp.getName(),
                                                                pp.getLocale(),
                                                                pp.getServer()));
                                            }
                                        });
                    } else if (packet instanceof PlayerEvent event) {
                        handlePlayerEvent(event);
                    } else if (packet instanceof TeleportRequest request) {
                        handleTeleport(request);
                    } else if (packet instanceof ProxiumNode sn) {
                        setNode(sn);
                    } else if (packet instanceof RequestPacket
                            || packet instanceof ResponsePacket) {
                        handleBridgePacket(packet);
                    }
                });
    }

    private void handlePlayerEvent(PlayerEvent event) {
        kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer pp = event.player();
        kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer local = players.get(pp.getUniqueId());

        switch (event.action()) {
            case JOIN -> {
                trackServer(pp.getServer());
                if (local == null) {
                    local =
                            new kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer(
                                    this,
                                    pp.getUniqueId(),
                                    pp.getName(),
                                    pp.getLocale(),
                                    pp.getServer());
                    players.put(local.getUniqueId(), local);
                }
            }
            case SWITCH -> {
                trackServer(pp.getServer());
                if (local != null) {
                    local.setServer(pp.getServer());
                    local.setLocale(pp.getLocale());
                } else {
                    local =
                            new kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer(
                                    this,
                                    pp.getUniqueId(),
                                    pp.getName(),
                                    pp.getLocale(),
                                    pp.getServer());
                    players.put(local.getUniqueId(), local);
                }
            }
            case LEAVE -> players.remove(pp.getUniqueId());
        }
    }

    private void handleTeleport(TeleportRequest request) {
        Player player = Bukkit.getPlayer(request.player().getUniqueId());
        if (player == null) return;

        Location location = null;

        if (request.targetLocation() != null) {
            ProxyLocation loc = request.targetLocation();
            World world = Bukkit.getWorld(loc.world());
            if (world != null) {
                location = new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
            }
        } else if (request.targetPlayer() != null) {
            Player target = Bukkit.getPlayer(request.targetPlayer().getUniqueId());
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

    /** ProxyLocation을 Bukkit Location으로 변환한다. */
    public static Location toBukkit(ProxyLocation loc) {
        World world = Bukkit.getWorld(loc.world());
        if (world == null) return null;
        return new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
    }
}
