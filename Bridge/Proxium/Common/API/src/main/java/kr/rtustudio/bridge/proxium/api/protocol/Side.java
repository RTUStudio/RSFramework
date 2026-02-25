package kr.rtustudio.bridge.proxium.api.protocol;

public enum Side {
    PROXY,
    SERVER;

    public boolean isProxy() {
        return this == PROXY;
    }
}
