package kr.rtustudio.bridge.protoweaver.bukkit.core;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.netty.Sender;
import kr.rtustudio.bridge.protoweaver.api.protocol.CompressionType;
import kr.rtustudio.bridge.protoweaver.api.protocol.Protocol;
import kr.rtustudio.bridge.protoweaver.api.protocol.internal.*;
import kr.rtustudio.bridge.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.bridge.protoweaver.core.protocol.protoweaver.ProxyPacketHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class ProtoWeaver implements kr.rtustudio.bridge.protoweaver.bukkit.api.ProtoWeaver {

    private final Security security;
    private final BridgeOptions options;

    private final Set<ProtocolRegister> protocols = ConcurrentHashMap.newKeySet();
    private final Set<ProtocolRegister> unregistered = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ProxyPlayer> players = new Reference2ObjectOpenHashMap<>();
    private final Map<BridgeChannel, Boolean> registeredChannels = new ConcurrentHashMap<>();
    private final Map<BridgeChannel, Consumer<Object>> channelHandlers = new ConcurrentHashMap<>();

    private String server = "Standalone Server";
    private volatile ProtoConnection connection;

    public ProtoWeaver(String sslFolder, BridgeOptions options) {
        this.security = new Security(sslFolder);
        this.security.setup(options.isTls());
        this.options = options;

        Protocol.Builder protocol = Protocol.create(BridgeChannel.INTERNAL);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb

        protocol.addPacket(byte[].class);
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(SendMessage.class);
        protocol.addPacket(Broadcast.class);
        protocol.addPacket(ServerName.class);
        protocol.addPacket(PlayerList.class);
        protocol.addPacket(LocationTeleport.class);
        protocol.addPacket(PlayerTeleport.class);

        if (this.security.isModernProxy()) {
            protocol.setServerAuthHandler(VelocityAuth.class);
        }

        protocol.setServerHandler(ProtoHandler.class, this).load();
    }

    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public void register(BridgeChannel channel, Class<?>... types) {
        for (Class<?> type : types) {
            options.register(type);
        }
        registeredChannels.put(channel, true);
        loadChannelProtocol(channel);
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        channelHandlers.put(channel, handler);
        if (!registeredChannels.containsKey(channel)) {
            loadChannelProtocol(channel);
        }
    }

    @Override
    public void publish(BridgeChannel channel, Object message) {
        if (connection == null) {
            log.warn("ProtoWeaver not connected, cannot publish to channel: {}", channel);
            return;
        }
        if (!registeredChannels.containsKey(channel)) {
            log.warn(
                    "No codec registered for channel: {}. Call register() before publish().",
                    channel);
            return;
        }
        connection.send(options.encode(channel, message));
    }

    @Override
    public void unsubscribe(BridgeChannel channel) {
        channelHandlers.remove(channel);
        registeredChannels.remove(channel);
    }

    @Override
    public void close() {
        channelHandlers.clear();
        registeredChannels.clear();
        protocols.clear();
        unregistered.clear();
        connection = null;
    }

    @Override
    public boolean send(@NonNull InternalPacket packet) {
        ProtoConnection conn = connection;
        if (conn == null) return false;
        return conn.send(packet).isSuccess();
    }

    @Override
    public void ready(ProtoConnection conn) {
        if (connection != null) {
            for (ProtocolRegister protocol : protocols) {
                if (!conn.send(protocol).isSuccess()) {
                    unregistered.add(protocol);
                }
            }
        }
        connection = conn;
        unregistered.removeIf(protocol -> connection.send(protocol).isSuccess());

        Consumer<Object> systemHandler =
                channelHandlers.get(BridgeChannel.of("rsf:system:connection"));
        if (systemHandler != null) {
            systemHandler.accept(conn);
        }

        subscribe(
                BridgeChannel.INTERNAL,
                packet -> {
                    if (packet instanceof byte[] frame) {
                        dispatchFrame(frame);
                    } else if (packet instanceof PlayerList(Map<UUID, ProxyPlayer> map)) {
                        this.players.clear();
                        this.players.putAll(map);
                    } else if (packet instanceof TeleportRequest request) {
                        handleTeleport(request);
                    } else if (packet instanceof ServerName(String name)) {
                        this.server = name;
                        if (isConnected()) {
                            log.info("Server identified as {}!", name);
                        }
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

    private void dispatchFrame(byte[] frame) {
        BridgeChannel channel = options.peekChannel(frame);
        if (channel != null) {
            Consumer<Object> handler = channelHandlers.get(channel);
            if (registeredChannels.containsKey(channel) && handler != null) {
                try {
                    handler.accept(options.decode(frame));
                } catch (Exception e) {
                    log.error("Failed to decode frame on channel: {}", channel, e);
                }
                return;
            }
        }
        log.warn("Received unroutable ProtoWeaver frame (channel={})", channel);
    }

    private void loadChannelProtocol(BridgeChannel channel) {
        Protocol.Builder protocol = Protocol.create(channel);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(byte[].class);

        if (this.security.isModernProxy()) {
            protocol.setServerAuthHandler(VelocityAuth.class);
        }
        protocol.setServerHandler(ProxyPacketHandler.class).load();

        ProtocolRegister registry = new ProtocolRegister(channel, Set.of());
        protocols.add(registry);

        ProtoConnection conn = connection;
        if (conn != null) {
            Sender sender = conn.send(registry);
            if (!sender.isSuccess()) {
                unregistered.add(registry);
            }
        } else {
            unregistered.add(registry);
        }
    }
}
