package kr.rtustudio.broker.protoweaver.bukkit.core;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import kr.rtustudio.broker.BrokerOptions;
import kr.rtustudio.broker.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.broker.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.broker.protoweaver.api.netty.Sender;
import kr.rtustudio.broker.protoweaver.api.protocol.CompressionType;
import kr.rtustudio.broker.protoweaver.api.protocol.Protocol;
import kr.rtustudio.broker.protoweaver.api.protocol.internal.*;
import kr.rtustudio.broker.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyLocation;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.broker.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.broker.protoweaver.bukkit.api.nms.IProtoWeaver;
import kr.rtustudio.broker.protoweaver.core.protocol.protoweaver.ProxyPacketHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class ProtoWeaver implements kr.rtustudio.broker.protoweaver.bukkit.api.ProtoWeaver {

    private final IProtoWeaver protoWeaver;
    private final HandlerCallback callback;
    private final BrokerOptions options;
    private final boolean isModernProxy;

    private final Set<ProtocolRegister> protocols = ConcurrentHashMap.newKeySet();
    private final Set<ProtocolRegister> unregistered = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ProxyPlayer> players = new Reference2ObjectOpenHashMap<>();
    private final Map<String, Boolean> registeredChannels = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Object>> channelHandlers = new ConcurrentHashMap<>();

    private String server = "Standalone Server";
    private volatile ProtoConnection connection;

    public ProtoWeaver(String sslFolder, HandlerCallback callback, ClassLoader classLoader) {
        this(sslFolder, callback, BrokerOptions.defaults(classLoader));
    }

    public ProtoWeaver(String sslFolder, HandlerCallback callback, BrokerOptions options) {
        this.protoWeaver = new ProtoWeaverSetup(sslFolder);
        this.protoWeaver.setup(options.isTls());
        this.callback = callback;
        this.options = options;
        this.isModernProxy = this.protoWeaver.isModernProxy();

        Protocol.Builder protocol = Protocol.create("rsframework", "internal");
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

        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
        }

        protocol.setServerHandler(ProtoHandler.class, this.callback).load();
    }

    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public void register(String channel, Class<?>... types) {
        for (Class<?> type : types) {
            options.register(type);
        }
        registeredChannels.put(channel, true);
        loadChannelProtocol(channel);
    }

    @Override
    public void subscribe(String channel, Consumer<Object> handler) {
        channelHandlers.put(channel, handler);
        if (!registeredChannels.containsKey(channel)) {
            loadChannelProtocol(channel);
        }
    }

    @Override
    public void publish(String channel, Object message) {
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
    public void unsubscribe(String channel) {
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
    public boolean publish(@NonNull InternalPacket packet) {
        ProtoConnection conn = connection;
        if (conn == null) return false;
        return conn.send(packet).isSuccess();
    }

    public void onReady(HandlerCallback.Ready data) {
        ProtoConnection conn = data.protoConnection();
        if (connection != null) {
            for (ProtocolRegister protocol : protocols) {
                if (!conn.send(protocol).isSuccess()) {
                    unregistered.add(protocol);
                }
            }
        }
        connection = conn;
        unregistered.removeIf(protocol -> connection.send(protocol).isSuccess());
    }

    public void onPacket(HandlerCallback.Packet data) {
        Object packet = data.packet();

        if (packet instanceof byte[] frame) {
            dispatchFrame(frame);
        } else if (packet instanceof PlayerList(Map<UUID, ProxyPlayer> map)) {
            this.players.clear();
            this.players.putAll(map);
        } else if (packet instanceof TeleportRequest request) {
            handleTeleport(request);
        } else if (packet instanceof ServerName(String name)) {
            this.server = name;
        }
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

        if (protoWeaver.isPaper()) {
            player.teleportAsync(location);
        } else {
            player.teleport(location);
        }
    }

    private void dispatchFrame(byte[] frame) {
        String channel = options.peekChannel(frame);
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

    private void loadChannelProtocol(String channel) {
        String[] parts = channel.split(":", 2);
        String namespace = parts[0];
        String key = parts.length > 1 ? parts[1] : parts[0];

        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(byte[].class);

        if (isModernProxy) {
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
