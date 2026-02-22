package kr.rtustudio.bridge.protoweaver.api.proxy;

public record ProxyLocation(
        String server, String world, double x, double y, double z, float yaw, float pitch) {

    public ProxyLocation(ProtoServer server, String world, double x, double y, double z) {
        this(server.name(), world, x, y, z, 0, 0);
    }
}
