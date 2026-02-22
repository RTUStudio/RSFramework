package kr.rtustudio.bridge.protoweaver.api.proxy.request.teleport;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.TeleportRequest;

public record PlayerTeleport(ProxyPlayer player, ProxyPlayer target) implements TeleportRequest {

    public String server() {
        return target.server();
    }
}
