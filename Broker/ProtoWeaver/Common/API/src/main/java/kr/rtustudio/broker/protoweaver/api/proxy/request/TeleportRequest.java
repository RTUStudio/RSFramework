package kr.rtustudio.broker.protoweaver.api.proxy.request;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;

public interface TeleportRequest extends Request {

    ProxyPlayer player();

    String server();
}
