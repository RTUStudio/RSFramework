package kr.rtuserver.protoweaver.api.protocol.internal;

import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;

import java.util.Map;
import java.util.UUID;

public record PlayerList(Map<UUID, ProxyPlayer> players) implements InternalPacket {}
