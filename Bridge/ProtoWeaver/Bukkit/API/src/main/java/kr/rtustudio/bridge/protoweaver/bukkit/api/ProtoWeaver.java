package kr.rtustudio.bridge.protoweaver.bukkit.api;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public interface ProtoWeaver extends Bridge {

    String getServer();

    Security getSecurity();

    @NotNull
    Map<UUID, ProxyPlayer> getPlayers();

    boolean isConnected();

    boolean send(@NotNull InternalPacket packet);

    void ready(ProtoConnection connection);
}
