package kr.rtustudio.bridge.proxium.api.proxy.request.teleport;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;

public record PlayerTeleport(ProxyPlayer player, ProxyPlayer target) implements TeleportRequest {

    public String server() {
        return target.server();
    }
}
