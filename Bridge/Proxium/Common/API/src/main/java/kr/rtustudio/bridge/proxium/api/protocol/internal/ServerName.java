package kr.rtustudio.bridge.proxium.api.protocol.internal;

public record ServerName(String name, String platform) {

    public ServerName(String name) {
        this(name, "Unknown");
    }
}
