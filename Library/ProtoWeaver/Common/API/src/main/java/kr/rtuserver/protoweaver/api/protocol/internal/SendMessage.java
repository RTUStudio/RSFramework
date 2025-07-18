package kr.rtuserver.protoweaver.api.protocol.internal;

import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;

public record SendMessage(ProxyPlayer player, String minimessage) implements GlobalPacket {
}
