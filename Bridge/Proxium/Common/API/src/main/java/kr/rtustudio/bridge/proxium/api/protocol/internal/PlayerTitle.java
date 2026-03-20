package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

public record PlayerTitle(
        ProxyPlayer player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {}
