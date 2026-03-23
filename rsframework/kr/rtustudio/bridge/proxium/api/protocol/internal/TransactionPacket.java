package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;

import java.util.UUID;

public interface TransactionPacket {
    UUID requestId();

    String sender();

    String target();

    BridgeChannel channel();

    Object payload();
}
