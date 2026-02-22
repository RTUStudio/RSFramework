package kr.rtustudio.bridge.protoweaver.api.proxy.request;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;

public interface TeleportRequest extends Request {

    ProxyPlayer player();

    String server();
}
