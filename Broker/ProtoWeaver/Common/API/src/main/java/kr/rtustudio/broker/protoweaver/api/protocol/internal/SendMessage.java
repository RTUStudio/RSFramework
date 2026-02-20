package kr.rtustudio.broker.protoweaver.api.protocol.internal;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;

public record SendMessage(ProxyPlayer player, String minimessage) implements GlobalPacket {}
