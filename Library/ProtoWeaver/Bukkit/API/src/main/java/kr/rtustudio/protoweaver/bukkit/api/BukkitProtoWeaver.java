package kr.rtustudio.protoweaver.bukkit.api;

import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.protoweaver.api.protocol.Packet;
import kr.rtustudio.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.protoweaver.bukkit.api.nms.IProtoWeaver;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public interface BukkitProtoWeaver {

    String getServer();

    IProtoWeaver getProtoWeaver();

    @NotNull
    Map<UUID, ProxyPlayer> getPlayers();

    boolean isConnected();

    boolean isModernProxy();

    void registerProtocol(
            String namespace,
            String key,
            Packet packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback);

    void registerProtocol(
            String namespace,
            String key,
            Set<Packet> packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback);

    boolean sendPacket(InternalPacket packet);
}
