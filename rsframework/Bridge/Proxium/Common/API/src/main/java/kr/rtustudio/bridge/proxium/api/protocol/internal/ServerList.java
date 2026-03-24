package kr.rtustudio.bridge.proxium.api.protocol.internal;

import kr.rtustudio.bridge.proxium.api.ProxiumNode;

import java.util.Map;

public record ServerList(Map<String, ProxiumNode> servers) {}
