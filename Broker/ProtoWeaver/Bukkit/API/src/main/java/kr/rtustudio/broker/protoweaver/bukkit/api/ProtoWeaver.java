package kr.rtustudio.broker.protoweaver.bukkit.api;

import kr.rtustudio.broker.Broker;
import kr.rtustudio.broker.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.broker.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.broker.protoweaver.bukkit.api.nms.IProtoWeaver;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public interface ProtoWeaver extends Broker {

    String getServer();

    IProtoWeaver getProtoWeaver();

    @NotNull
    Map<UUID, ProxyPlayer> getPlayers();

    boolean isConnected();

    boolean isModernProxy();

    boolean publish(@NotNull InternalPacket packet);

    void onReady(HandlerCallback.Ready data);

    void onPacket(HandlerCallback.Packet data);
}
