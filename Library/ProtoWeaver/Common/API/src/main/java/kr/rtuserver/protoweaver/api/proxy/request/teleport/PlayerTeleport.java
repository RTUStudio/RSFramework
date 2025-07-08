package kr.rtuserver.protoweaver.api.proxy.request.teleport;

import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;

public record PlayerTeleport(ProxyPlayer player, ProxyPlayer location) implements TeleportRequest {

    public String server() {
        return location.getServer();
    }

}