package kr.rtustudio.bridge.proxium.api.proxy.request.teleport;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;

public record LocationTeleport(ProxyPlayer player, ProxyLocation location)
        implements TeleportRequest {

    public String server() {
        return location.server();
    }
}
