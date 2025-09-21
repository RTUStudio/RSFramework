package kr.rtustudio.protoweaver.api.protocol.internal;

import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;

public record SendMessage(ProxyPlayer player, String minimessage) implements GlobalPacket {}
