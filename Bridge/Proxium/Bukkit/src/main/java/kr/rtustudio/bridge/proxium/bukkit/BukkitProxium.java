package kr.rtustudio.bridge.proxium.bukkit;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.configuration.ProxiumConfig;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.*;
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

@Slf4j(topic = "Proxium")
@Getter
public class BukkitProxium extends ProxiumServer {

    private final Security security;
    private boolean loaded = false;

    public BukkitProxium(ClassLoader classLoader, String sslFolder, ProxiumConfig settings) {
        super(new BridgeOptions(classLoader), settings);
        this.security = new Security(sslFolder);
        this.security.setup(settings.getTls().isEnabled());

        try {
            register(
                    BridgeChannel.INTERNAL,
                    BridgeChannel.class,
                    ProxiumNode.class,
                    PlayerList.class,
                    ServerList.class,
                    TeleportRequest.class,
                    PlayerEvent.class,
                    MutableProxyPlayer.class,
                    RequestPacket.class,
                    ResponsePacket.class,
                    BroadcastMessage.class);
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

    /** ProxyLocation을 Bukkit Location으로 변환한다. */
    public static Location toBukkit(ProxyLocation loc) {
        World world = Bukkit.getWorld(loc.world());
        if (world == null) return null;
        return new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    private void registerInternalSubscription() {
        subscribe(BridgeChannel.INTERNAL, PlayerList.class, this::handlePlayerList);
        subscribe(BridgeChannel.INTERNAL, ServerList.class, this::handleServerList);
        subscribe(BridgeChannel.INTERNAL, PlayerEvent.class, this::handlePlayerEvent);
        subscribe(BridgeChannel.INTERNAL, ProxiumNode.class, this::setNode);
    }

    private void handlePlayerList(PlayerList playerList) {
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
}
