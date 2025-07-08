package kr.rtuserver.protoweaver.api.proxy.request;

import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;

public interface TeleportRequest extends Request {

    ProxyPlayer player();

    String server();

}

