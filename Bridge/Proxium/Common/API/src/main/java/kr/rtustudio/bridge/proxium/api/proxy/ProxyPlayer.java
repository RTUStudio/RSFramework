package kr.rtustudio.bridge.proxium.api.proxy;

import java.util.Locale;
import java.util.UUID;

public record ProxyPlayer(UUID uniqueId, String name, Locale locale, String server) {}
