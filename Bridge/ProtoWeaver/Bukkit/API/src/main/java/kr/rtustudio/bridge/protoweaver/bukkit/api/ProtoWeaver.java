package kr.rtustudio.bridge.protoweaver.bukkit.api;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.bridge.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.protoweaver.bukkit.api.nms.IProtoWeaver;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public interface ProtoWeaver extends Bridge {

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
