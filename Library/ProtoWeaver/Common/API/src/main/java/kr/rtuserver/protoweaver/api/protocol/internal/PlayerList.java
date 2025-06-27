package kr.rtuserver.protoweaver.api.protocol.internal;

import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;

import java.util.List;

public record PlayerList(List<ProxyPlayer> players) implements InternalPacket {
}
