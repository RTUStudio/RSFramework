package kr.rtustudio.protoweaver.api.protocol.internal;

import kr.rtustudio.protoweaver.api.protocol.Packet;

import java.util.Set;

public record ProtocolRegister(String namespace, String key, Set<Packet> packets)
        implements InternalPacket {

    public ProtocolRegister(String namespace, String key, Packet packet) {
        this(namespace, key, Set.of(packet));
    }
}
