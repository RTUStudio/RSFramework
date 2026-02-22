package kr.rtustudio.bridge.protoweaver.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.protoweaver.api.protocol.Packet;

import java.util.Set;

public record ProtocolRegister(BridgeChannel channel, Set<Packet> packets)
        implements InternalPacket {

    public ProtocolRegister(BridgeChannel channel, Packet packet) {
        this(channel, Set.of(packet));
    }
}
