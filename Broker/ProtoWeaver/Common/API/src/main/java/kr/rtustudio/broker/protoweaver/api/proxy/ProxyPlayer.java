package kr.rtustudio.broker.protoweaver.api.proxy;

import kr.rtustudio.broker.protoweaver.api.protocol.internal.InternalPacket;

import java.util.Locale;
import java.util.UUID;

public record ProxyPlayer(UUID uniqueId, String name, Locale locale, String server)
        implements InternalPacket {}
