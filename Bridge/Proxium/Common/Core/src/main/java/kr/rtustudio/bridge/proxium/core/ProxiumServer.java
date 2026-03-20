package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * 서버 측(Bukkit 등) Proxium 플랫폼 기반 클래스.
 *
 * <p>프록시와의 단일 커넥션을 관리하고, 플레이어 목록 수신 및 패킷 송수신을 담당한다.
 */
@Slf4j(topic = "Proxium")
@Getter
public abstract class ProxiumServer extends AbstractProxium {

    private static final java.time.Duration DEFAULT_REQUEST_TIMEOUT = java.time.Duration.ofSeconds(5);

    private final Map<String, ProxiumNode> knownServers = new ConcurrentHashMap<>();

    /** 현재 서버 노드 (이름 + 주소). 프록시 연결 후 프록시가 전달한 ProxiumNode로 설정된다. */
    @Setter @Nullable private ProxiumNode node;

    /** 연결된 프록시 노드. 프록시 연결 후 설정된다. */
    @Nullable private ProxiumNode proxy;

    @Nullable private Connection connection;

    protected ProxiumServer(BridgeOptions options) {
        super(options);
    }

    @Override
    public java.time.Duration getRequestTimeout() {
        return DEFAULT_REQUEST_TIMEOUT;
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

    @Override
    protected void dispatchOutboundPacket(Object packet) {
        send(packet);
    }

    public void handleBridgePacket(Object packetObj) {
        handleTransaction(packetObj);
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
        InetSocketAddress remoteAddr = (InetSocketAddress) connection.getRemoteAddress();
        this.proxy = new ProxiumNode("Proxy", remoteAddr.getHostString(), remoteAddr.getPort());
        send(BridgeChannel.INTERNAL);
    }

    @Override
    public void close() {
        channelHandlers.clear();
        registeredChannels.clear();
        connection = null;
        proxy = null;
        node = null;
    }
}
