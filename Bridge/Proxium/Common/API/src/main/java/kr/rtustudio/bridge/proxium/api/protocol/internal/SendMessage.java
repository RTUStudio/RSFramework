package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

public record SendMessage(ProxyPlayer player, String minimessage) {}
