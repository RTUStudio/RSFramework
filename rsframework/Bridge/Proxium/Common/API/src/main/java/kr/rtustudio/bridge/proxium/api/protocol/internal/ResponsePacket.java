package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.ResponseStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ResponsePacket(
        UUID requestId,
        ProxiumNode sender,
        ProxiumNode target,
        BridgeChannel channel,
        ResponseStatus status,
        Object payload)
        implements TransactionPacket {}
