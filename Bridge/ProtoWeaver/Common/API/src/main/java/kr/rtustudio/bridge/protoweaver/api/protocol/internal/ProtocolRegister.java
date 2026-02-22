package kr.rtustudio.bridge.protoweaver.api.protocol.internal;

import kr.rtustudio.bridge.protoweaver.api.protocol.Packet;

import java.util.Set;

public record ProtocolRegister(String channel, Set<Packet> packets) implements InternalPacket {

    public ProtocolRegister(String channel, Packet packet) {
        this(channel, Set.of(packet));
    }
}
