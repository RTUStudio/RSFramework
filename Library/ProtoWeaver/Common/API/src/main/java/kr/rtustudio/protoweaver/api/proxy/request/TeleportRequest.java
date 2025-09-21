package kr.rtustudio.protoweaver.api.proxy.request;

import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;

public interface TeleportRequest extends Request {

    ProxyPlayer player();

    String server();
}
