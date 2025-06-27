package kr.rtuserver.protoweaver.api.proxy;

import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;

import java.util.UUID;

public record ProxyPlayer(UUID uuid, String name) implements InternalPacket {
}
