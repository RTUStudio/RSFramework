package kr.rtustudio.bridge.proxium.api;

public record ProxiumNode(String name, String address) {
    @Override
    public String toString() {
        return name + (address != null ? " : " + address : "");
    }
}
