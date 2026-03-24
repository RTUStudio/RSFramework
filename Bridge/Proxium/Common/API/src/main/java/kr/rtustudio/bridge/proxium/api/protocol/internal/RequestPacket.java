package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RequestPacket(
        UUID requestId,
        ProxiumNode sender,
        ProxiumNode target,
        BridgeChannel channel,
        byte[] payload)
        implements TransactionPacket {}
