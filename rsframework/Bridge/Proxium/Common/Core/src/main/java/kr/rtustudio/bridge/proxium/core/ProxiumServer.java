package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * 서버 측(Bukkit 등) Proxium 플랫폼 기반 클래스.
 *
 * <p>프록시와의 단일 커넥션을 관리하고, 플레이어 목록 수신 및 패킷 송수신을 담당한다.
 */
@Slf4j(topic = "Proxium")
@Getter
public abstract class ProxiumServer extends AbstractProxium {

    private static final ProxiumNode STANDALONE_NODE = new ProxiumNode("Standalone Server", null);

    protected volatile Connection connection;
    protected final Map<String, ProxiumNode> knownServers = new ConcurrentHashMap<>();
    @Setter protected ProxiumNode node = null;
    private ProxiumNode proxy = null;

    protected ProxiumServer(BridgeOptions options) {
        super(options);
    }

    @Override
    public boolean isConnected() {
        Connection conn = connection;
        return conn != null && conn.isOpen();
    }

    @Override
    public String getServer() {
        return node != null ? node.name() : null;
    }

    @Override
    public ProxiumNode getServer(String name) {
        return knownServers.get(name);
    }

    /** 알려진 서버 목록에 서버를 등록한다. 플레이어 데이터로부터 자동 호출. */
    protected void trackServer(String name) {
        knownServers.computeIfAbsent(name, n -> new ProxiumNode(n, null));
    }

    @Override
    protected void dispatchOutboundPacket(Object packet) {
        send(packet);
    }

    public void handleBridgePacket(Object packetObj) {
        handleRpcPacket(packetObj);
    }

    @Override
    public boolean send(@NotNull Object packet) {
        Connection conn = connection;
        if (conn == null) return false;
        return conn.send(options.encode(BridgeChannel.INTERNAL, packet)).isSuccess();
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        super.subscribe(channel, handler);
        if (connection != null) {
            send(channel);
        }
    }

    @Override
    public void publish(BridgeChannel channel, Object message) {
        if (!registeredChannels.contains(channel)) {
            log.warn(
                    "No codec registered for channel: {}. Call register() before publish().",
                    channel);
            return;
        }
        Connection conn = connection;
        if (conn == null) {
            log.warn("Cannot publish to channel {}: not connected to proxy", channel);
            return;
        }
        conn.send(options.encode(channel, message));
    }

    @Override
    public void ready(Connection connection) {
        this.connection = connection;
        this.proxy = new ProxiumNode("Proxy", connection.getRemoteAddress().toString());
        send(BridgeChannel.INTERNAL);

        Consumer<Object> systemHandler =
                channelHandlers.get(BridgeChannel.of("rsf:system:connection"));
        if (systemHandler != null) {
            systemHandler.accept(connection);
        }
    }

    @Override
    public void close() {
        channelHandlers.clear();
        registeredChannels.clear();
        connection = null;
        node = STANDALONE_NODE;
    }
}
