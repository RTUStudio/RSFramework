package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RequestPacket(
        UUID requestId, String sender, String target, BridgeChannel channel, Object payload)
        implements TransactionPacket {}
