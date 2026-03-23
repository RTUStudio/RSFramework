package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.exception.ResponseStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ResponsePacket(
        UUID requestId,
        String sender,
        String target,
        BridgeChannel channel,
        ResponseStatus status,
        Object payload)
        implements TransactionPacket {}
