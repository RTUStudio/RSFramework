package kr.rtustudio.bridge.protoweaver.bukkit.core;

import kr.rtustudio.bridge.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RSF/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class ProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection proxy;
    private final ProtoWeaver protoWeaver;

    public static ProtoConnection getProxy() {
        if (proxy == null || !proxy.isOpen()) return null;
        return proxy;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        log.info("Connected to Proxy");
        log.info("┠ Address: {}", protoConnection.getRemoteAddress());
        log.info("┖ Protocol: {}", protoConnection.getProtocol().getNamespaceKey());
        if (protoWeaver != null) protoWeaver.ready(protoConnection);
        proxy = protoConnection;
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (protoWeaver != null) {
            java.util.function.Consumer<Object> handler =
                    protoWeaver
                            .getChannelHandlers()
                            .get(protoConnection.getProtocol().getNamespaceKey());
            if (handler != null) {
                handler.accept(packet);
            }
        }
    }
}
