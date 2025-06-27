package kr.rtuserver.protoweaver.api.proxy.request;

import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtuserver.protoweaver.api.proxy.ProxyLocation;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;

public record TeleportRequest(ProxyPlayer player, ProxyLocation location) implements Request, InternalPacket {
}

