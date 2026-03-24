package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;

import java.util.UUID;

public interface TransactionPacket {
    UUID requestId();

    ProxiumNode sender();

    ProxiumNode target();

    BridgeChannel channel();

    byte[] payload();
}
