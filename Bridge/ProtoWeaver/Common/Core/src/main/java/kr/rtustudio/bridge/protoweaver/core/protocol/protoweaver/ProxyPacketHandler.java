package kr.rtustudio.bridge.protoweaver.core.protocol.protoweaver;

import kr.rtustudio.bridge.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.bridge.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RSF/ProtoHandler")
@RequiredArgsConstructor
public class ProxyPacketHandler implements ProtoConnectionHandler {

    private static ProtoConnection proxy;
    private final HandlerCallback callable;

    public static ProtoConnection getProxy() {
        if (proxy == null || !proxy.isOpen()) return null;
        return proxy;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        log.info("Connected to Proxy");
        log.info("┠ Address: {}", protoConnection.getRemoteAddress());
        log.info("┖ Protocol: {}", protoConnection.getProtocol().getNamespaceKey());
        if (callable != null) callable.onReady(protoConnection);
        proxy = protoConnection;
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
    }
}
