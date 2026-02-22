package kr.rtustudio.bridge.protoweaver.api.protocol.internal;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;

public record SendMessage(ProxyPlayer player, String minimessage) implements GlobalPacket {}
