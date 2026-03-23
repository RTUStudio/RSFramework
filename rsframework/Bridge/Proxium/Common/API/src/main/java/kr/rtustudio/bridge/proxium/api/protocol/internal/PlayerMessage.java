package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

public record PlayerMessage(ProxyPlayer player, String message) {}
