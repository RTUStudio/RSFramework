package kr.rtuserver.protoweaver.api.proxy.request.teleport;

import kr.rtuserver.protoweaver.api.proxy.ProxyLocation;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;

public record LocationTeleport(ProxyPlayer player, ProxyLocation location)
        implements TeleportRequest {

    public String server() {
        return location.server();
    }
}
