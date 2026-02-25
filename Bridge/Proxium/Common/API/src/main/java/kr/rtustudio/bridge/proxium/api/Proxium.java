package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public interface Proxium extends Bridge {

    boolean isLoaded();

    boolean isConnected();

    @NotNull
    Map<UUID, ProxyPlayer> getPlayers();

    String getServer();

    boolean send(@NotNull Object packet);

    void ready(Connection connection);

    default boolean teleport(TeleportRequest request) {
        return send(request);
    }
}
