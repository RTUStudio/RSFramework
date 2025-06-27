package kr.rtuserver.protoweaver.bukkit.api;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.bukkit.api.nms.IProtoWeaver;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface BukkitProtoWeaver {

    String getServer();

    IProtoWeaver getProtoWeaver();

    Map<UUID, ProxyPlayer> getPlayers();

    boolean isConnected();

    boolean isModernProxy();

    void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

    void registerProtocol(String namespace, String key, Set<Packet> packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

    boolean sendPacket(InternalPacket packet);
}