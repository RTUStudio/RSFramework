package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

import java.util.Map;
import java.util.UUID;

public record PlayerList(Map<UUID, ProxyPlayer> players) {}
