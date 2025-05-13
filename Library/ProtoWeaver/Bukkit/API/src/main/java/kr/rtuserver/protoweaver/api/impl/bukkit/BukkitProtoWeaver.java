package kr.rtuserver.protoweaver.api.impl.bukkit;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.ProxyPlayer;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.impl.bukkit.nms.IProtoWeaver;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;

import java.util.Set;

public interface BukkitProtoWeaver {

    IProtoWeaver getProtoWeaver();

    Set<ProxyPlayer> getPlayers();

    boolean isConnected();

    boolean isModernProxy();

    void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

    boolean sendPacket(InternalPacket packet);
}