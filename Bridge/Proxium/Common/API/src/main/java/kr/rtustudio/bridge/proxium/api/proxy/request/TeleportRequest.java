package kr.rtustudio.bridge.proxium.api.proxy.request;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

public interface TeleportRequest extends Request {

    ProxyPlayer player();

    String server();
}
