package kr.rtuserver.protoweaver.core.impl.bukkit;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.ProxyPlayer;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.impl.bukkit.BukkitProtoHandler;
import kr.rtuserver.protoweaver.api.impl.bukkit.nms.IProtoWeaver;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.netty.Sender;
import kr.rtuserver.protoweaver.api.protocol.CompressionType;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import kr.rtuserver.protoweaver.api.protocol.internal.*;
import kr.rtuserver.protoweaver.api.protocol.serializer.CustomPacketSerializer;
import kr.rtuserver.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_17_r1.ProtoWeaver_1_17_R1;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_18_r1.ProtoWeaver_1_18_R1;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_18_r2.ProtoWeaver_1_18_R2;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_19_r1.ProtoWeaver_1_19_R1;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_19_r2.ProtoWeaver_1_19_R2;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_19_r3.ProtoWeaver_1_19_R3;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_20_r1.ProtoWeaver_1_20_R1;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_20_r2.ProtoWeaver_1_20_R2;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_20_r3.ProtoWeaver_1_20_R3;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_20_r4.ProtoWeaver_1_20_R4;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_21_r1.ProtoWeaver_1_21_R1;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_21_r2.ProtoWeaver_1_21_R2;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_21_r3.ProtoWeaver_1_21_R3;
import kr.rtuserver.protoweaver.core.impl.bukkit.nms.v1_21_r4.ProtoWeaver_1_21_R4;
import kr.rtuserver.protoweaver.core.protocol.protoweaver.ProxyPacketHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class BukkitProtoWeaver implements kr.rtuserver.protoweaver.api.impl.bukkit.BukkitProtoWeaver {

    private final IProtoWeaver protoWeaver;
    private final HandlerCallback callback;
    private final boolean isModernProxy;
    private final Set<ProtocolRegister> protocols = new HashSet<>();
    private final Set<ProtocolRegister> unregistered = new HashSet<>();
    private final Set<ProxyPlayer> players = new HashSet<>();
    private ProtoConnection connection;

    public BukkitProtoWeaver(String sslFolder, String nmsVersion, HandlerCallback callback) {
        this.protoWeaver = switch (nmsVersion) {
            case "v1_17_R1" -> new ProtoWeaver_1_17_R1(sslFolder);
            case "v1_18_R1" -> new ProtoWeaver_1_18_R1(sslFolder);
            case "v1_18_R2" -> new ProtoWeaver_1_18_R2(sslFolder);
            case "v1_19_R1" -> new ProtoWeaver_1_19_R1(sslFolder);
            case "v1_19_R2" -> new ProtoWeaver_1_19_R2(sslFolder);
            case "v1_19_R3" -> new ProtoWeaver_1_19_R3(sslFolder);
            case "v1_20_R1" -> new ProtoWeaver_1_20_R1(sslFolder);
            case "v1_20_R2" -> new ProtoWeaver_1_20_R2(sslFolder);
            case "v1_20_R3" -> new ProtoWeaver_1_20_R3(sslFolder);
            case "v1_20_R4" -> new ProtoWeaver_1_20_R4(sslFolder);
            case "v1_21_R1" -> new ProtoWeaver_1_21_R1(sslFolder);
            case "v1_21_R2" -> new ProtoWeaver_1_21_R2(sslFolder);
            case "v1_21_R3" -> new ProtoWeaver_1_21_R3(sslFolder);
            case "v1_21_R4" -> new ProtoWeaver_1_21_R4(sslFolder);
            default -> throw new IllegalStateException();
        };
        this.callback = callback;
        this.isModernProxy = protoWeaver.isModernProxy();
        Protocol.Builder protocol = Protocol.create("rsframework", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(Packet.class);

        protocol.addPacket(ProxyPlayer.class);
        protocol.addPacket(PlayerList.class);
        protocol.addPacket(StorageSync.class);
        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
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
//        if (connection != null) {
//            for (ProtocolRegister protocol : protocols) {
//                if (connection != null) {
//                    Sender sender = connection.send(protocol);
//                    if (sender.isSuccess()) log.info("Protocol({}) is reconnected", protocol.namespace() + ":" + protocol.key());
//                } else unregistered.add(protocol);
//            }
//        }
        connection = data.protoConnection();
        Set<ProtocolRegister> toRemove = new HashSet<>();
        for (ProtocolRegister protocol : unregistered) {
            Sender sender = connection.send(protocol);
            if (sender.isSuccess()) toRemove.add(protocol);
        }
        unregistered.removeAll(toRemove);
    }

    public void onPacket(HandlerCallback.Packet packet) {
        if (packet.packet() instanceof PlayerList(List<ProxyPlayer> players1)) {
            players.clear();
            players.addAll(players1);
        }
    }

    public void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        registerProtocol(namespace, key, Set.of(packet), protocolHandler, callback);
    }

    public void registerProtocol(String namespace, String key, Set<Packet> packets, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
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
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        if (protocolHandler == null) protocolHandler = ProxyPacketHandler.class;
        if (callback == null) protocol.setServerHandler(protocolHandler).load();
        else protocol.setServerHandler(protocolHandler, callback).load();

        Set<Packet> result = new HashSet<>();
        for (Packet packet : packets) {
            if (packet.getSerializer() != CustomPacketSerializer.class) result.add(packet);
        }
        ProtocolRegister registry = new ProtocolRegister(namespace, key, result);
        if (connection != null) {
            Sender sender = connection.send(registry);
            if (sender.isSuccess()) log.info("New Protocol({}) is connected", namespace + ":" + key);
        } else unregistered.add(registry);
    }
}