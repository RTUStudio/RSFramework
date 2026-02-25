package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

/**
 * 서버 측(Bukkit 등) Proxium 플랫폼 기반 클래스.
 *
 * <p>프록시와의 단일 커넥션을 관리하고, 플레이어 목록 수신 및 패킷 송수신을 담당한다.
 */
@Slf4j(topic = "Proxium")
@Getter
public abstract class ProxiumServer extends AbstractProxium implements Proxium {

    protected final Map<UUID, ProxyPlayer> players = new ConcurrentHashMap<>();
    protected volatile Connection connection;
    protected String serverName = "Standalone Server";

    protected ProxiumServer(BridgeOptions options) {
        super(options);
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public @NonNull Map<UUID, ProxyPlayer> getPlayers() {
        return Map.copyOf(players);
    }

    @Override
    public String getServer() {
        return serverName;
    }

    @Override
    public boolean send(@NonNull Object packet) {
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
        if (connection == null) {
            log.warn("Proxium not connected, cannot publish to channel: {}", channel);
            return;
        }
        if (!registeredChannels.contains(channel)) {
            log.warn(
                    "No codec registered for channel: {}. Call register() before publish().",
                    channel);
            return;
        }
        connection.send(options.encode(channel, message));
    }

    @Override
    public void ready(Connection connection) {
        this.connection = connection;
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
        serverName = "Standalone Server";
    }
}
