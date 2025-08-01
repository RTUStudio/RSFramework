package kr.rtuserver.protoweaver.api.proxy.request.teleport;

import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;

public record PlayerTeleport(ProxyPlayer player, ProxyPlayer target) implements TeleportRequest {

    public String server() {
        return target.server();
    }

}