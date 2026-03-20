package kr.rtustudio.bridge.proxium.bukkit;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig;
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
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.core.MutableProxyPlayer;
import kr.rtustudio.bridge.proxium.core.ProxiumServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Slf4j(topic = "Proxium")
@Getter
public class BukkitProxium extends ProxiumServer {

    private final Security security;
    private boolean loaded = false;

    public BukkitProxium(ClassLoader classLoader, String sslFolder, ProxiumConfig settings) {
        super(new BridgeOptions(classLoader));
        this.security = new Security(sslFolder);
        this.security.setup(settings.getTls().isEnabled());

        try {
            register(
                    BridgeChannel.INTERNAL,
                    BridgeChannel.class,
                    ProxiumNode.class,
                    PlayerList.class,
                    TeleportRequest.class,
                    PlayerEvent.class,
                    RequestPacket.class,
                    ResponsePacket.class);
            register(
                    BridgeChannel.AUDIENCE,
                    BridgeChannel.class,
                    PlayerMessage.class,
                    BroadcastMessage.class,
                    PlayerActionBar.class,
                    PlayerTitle.class);

            registerInternalSubscription();

            Protocol.Builder builder = createProtocol(BridgeChannel.INTERNAL, settings);
            builder.setServerHandler(() -> new ConnectionHandler(this));
            if (security.isModernProxy()) {
                builder.setServerAuthHandler(VelocityAuth::new);
            }
            builder.load();

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
                        Set<UUID> incomingUuids = playerList.players().keySet();
                        this.players.keySet().removeIf(uuid -> !incomingUuids.contains(uuid));

                        playerList
                                .players()
                                .forEach(
                                        (uuid, pp) -> {
                                            ProxyPlayer local = this.players.get(uuid);
                                            if (local != null) {
                                                ((MutableProxyPlayer) local).setNode(pp.getNode());
                                            } else {
                                                this.players.put(
                                                        uuid,
                                                        new MutableProxyPlayer(
                                                                this,
                                                                pp.getUniqueId(),
                                                                pp.getName(),
                                                                pp.getNode()));
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
        ProxyPlayer pp = event.player();
        ProxyPlayer local = players.get(pp.getUniqueId());

        switch (event.action()) {
            case JOIN -> {
                if (local == null) {
                    local =
                            new MutableProxyPlayer(
                                    this, pp.getUniqueId(), pp.getName(), pp.getNode());
                    players.put(local.getUniqueId(), local);
                }
            }
            case SWITCH -> {
                if (local != null) {
                    ((MutableProxyPlayer) local).setNode(pp.getNode());
                } else {
                    local =
                            new MutableProxyPlayer(
                                    this, pp.getUniqueId(), pp.getName(), pp.getNode());
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
