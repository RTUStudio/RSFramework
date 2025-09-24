package kr.rtustudio.protoweaver.bukkit.core;

import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.protoweaver.api.netty.Sender;
import kr.rtustudio.protoweaver.api.protocol.CompressionType;
import kr.rtustudio.protoweaver.api.protocol.Packet;
import kr.rtustudio.protoweaver.api.protocol.Protocol;
import kr.rtustudio.protoweaver.api.protocol.internal.*;
import kr.rtustudio.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.protoweaver.api.proxy.ProxyLocation;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtustudio.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.protoweaver.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.protoweaver.api.serializer.CustomPacketSerializer;
import kr.rtustudio.protoweaver.bukkit.api.nms.IProtoWeaver;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_20_r1.ProtoWeaver_1_20_R1;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_20_r2.ProtoWeaver_1_20_R2;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_20_r3.ProtoWeaver_1_20_R3;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_20_r4.ProtoWeaver_1_20_R4;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_21_r1.ProtoWeaver_1_21_R1;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_21_r2.ProtoWeaver_1_21_R2;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_21_r3.ProtoWeaver_1_21_R3;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_21_r4.ProtoWeaver_1_21_R4;
import kr.rtustudio.protoweaver.bukkit.core.nms.v1_21_r5.ProtoWeaver_1_21_R5;
import kr.rtustudio.protoweaver.core.protocol.protoweaver.ProxyPacketHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class BukkitProtoWeaver implements kr.rtustudio.protoweaver.bukkit.api.BukkitProtoWeaver {

    private final IProtoWeaver protoWeaver;
    private final HandlerCallback callback;
    private final boolean isModernProxy;
    private final Set<ProtocolRegister> protocols = new HashSet<>();
    private final Set<ProtocolRegister> unregistered = new HashSet<>();
    private final Map<UUID, ProxyPlayer> players = new HashMap<>();
    private String server = "Standalone Server";
    private ProtoConnection connection;

    public BukkitProtoWeaver(String sslFolder, String nmsVersion, HandlerCallback callback) {
        this.protoWeaver =
                switch (nmsVersion) {
                    case "v1_20_R1" -> new ProtoWeaver_1_20_R1(sslFolder);
                    case "v1_20_R2" -> new ProtoWeaver_1_20_R2(sslFolder);
                    case "v1_20_R3" -> new ProtoWeaver_1_20_R3(sslFolder);
                    case "v1_20_R4" -> new ProtoWeaver_1_20_R4(sslFolder);
                    case "v1_21_R1" -> new ProtoWeaver_1_21_R1(sslFolder);
                    case "v1_21_R2" -> new ProtoWeaver_1_21_R2(sslFolder);
                    case "v1_21_R3" -> new ProtoWeaver_1_21_R3(sslFolder);
                    case "v1_21_R4" -> new ProtoWeaver_1_21_R4(sslFolder);
                    case "v1_21_R5" -> new ProtoWeaver_1_21_R5(sslFolder);
                    default -> throw new IllegalStateException();
                };
        protoWeaver.setup();
        this.callback = callback;
        this.isModernProxy = protoWeaver.isModernProxy();
        Protocol.Builder protocol = Protocol.create("rsframework", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Packet.class);
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
        protocol.setServerHandler(BukkitProtoHandler.class, this.callback).load();
    }

    public boolean isConnected() {
        return connection != null;
    }

    public boolean sendPacket(InternalPacket packet) {
        if (connection == null) return false;
        return connection.send(packet).isSuccess();
    }

    public void onReady(HandlerCallback.Ready data) {
        if (connection != null) {
            for (ProtocolRegister protocol : protocols) {
                Sender sender = data.protoConnection().send(protocol);
                if (!sender.isSuccess()) unregistered.add(protocol);
            }
        }
        connection = data.protoConnection();
        Set<ProtocolRegister> toRemove = new HashSet<>();
        for (ProtocolRegister protocol : unregistered) {
            Sender sender = connection.send(protocol);
            if (sender.isSuccess()) toRemove.add(protocol);
        }
        unregistered.removeAll(toRemove);
    }

    public void onPacket(HandlerCallback.Packet data) {
        Object packet = data.packet();
        if (packet instanceof PlayerList(Map<UUID, ProxyPlayer> map)) {
            this.players.clear();
            this.players.putAll(map);
        } else if (packet instanceof TeleportRequest request) {
            Player player = Bukkit.getPlayer(request.player().uniqueId());
            if (player == null) return;
            Location location = null;
            if (request instanceof LocationTeleport lt) {
                ProxyLocation loc = lt.location();
                World world = Bukkit.getWorld(loc.world());
                if (world == null) return;
                location = new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
            } else if (request instanceof PlayerTeleport pt) {
                ProxyPlayer loc = pt.target();
                Player target = Bukkit.getPlayer(loc.uniqueId());
                if (target == null) return;
                location = target.getLocation();
            }
            if (location == null) return;
            if (protoWeaver.isPaper()) player.teleportAsync(location);
            else player.teleport(location);
        } else if (packet instanceof ServerName(String name)) {
            this.server = name;
        }
    }

    public void registerProtocol(
            String namespace,
            String key,
            Packet packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback) {
        registerProtocol(namespace, key, Set.of(packet), protocolHandler, callback);
    }

    public void registerProtocol(
            String namespace,
            String key,
            Set<Packet> packets,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        for (Packet packet : packets) {
            if (packet.getSerializer() != null) {
                protocol.addPacket(packet.getPacket(), packet.getSerializer());
            } else protocol.addPacket(packet.getPacket());
        }
        protocol.addPacket(CustomPacket.class, CustomPacketSerializer.class);
        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
        }
        if (protocolHandler == null) protocolHandler = ProxyPacketHandler.class;
        if (callback == null) protocol.setServerHandler(protocolHandler).load();
        else protocol.setServerHandler(protocolHandler, callback).load();

        Set<Packet> result = new HashSet<>();
        for (Packet packet : packets) {
            if (packet.getSerializer() != CustomPacketSerializer.class) result.add(packet);
        }
        ProtocolRegister registry = new ProtocolRegister(namespace, key, result);
        protocols.add(registry);
        if (connection != null) {
            Sender sender = connection.send(registry);
            if (!sender.isSuccess()) unregistered.add(registry);
        } else unregistered.add(registry);
    }
}
