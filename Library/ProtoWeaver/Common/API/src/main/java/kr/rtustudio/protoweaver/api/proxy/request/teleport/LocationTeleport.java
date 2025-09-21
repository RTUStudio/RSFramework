package kr.rtustudio.protoweaver.api.proxy.request.teleport;

import kr.rtustudio.protoweaver.api.proxy.ProxyLocation;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.protoweaver.api.proxy.request.TeleportRequest;

public record LocationTeleport(ProxyPlayer player, ProxyLocation location)
        implements TeleportRequest {

    public String server() {
        return location.server();
    }
}
