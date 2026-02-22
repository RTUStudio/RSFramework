package kr.rtustudio.bridge.protoweaver.api.protocol.internal;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;

import java.util.Map;
import java.util.UUID;

public record PlayerList(Map<UUID, ProxyPlayer> players) implements InternalPacket {}
