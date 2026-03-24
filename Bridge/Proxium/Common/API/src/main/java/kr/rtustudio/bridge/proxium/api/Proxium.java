package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.Broadcast;
import kr.rtustudio.bridge.Node;
import kr.rtustudio.bridge.Transaction;
import kr.rtustudio.bridge.context.RequestContext;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Proxium bridge interface for establishing point-to-point Netty connections with proxy nodes.
 *
 * <p>프록시 노드와 단일 지점(Point-to-Point) Netty 커넥션을 맺고 통신하는 Proxium 브릿지 인터페이스.
 */
public interface Proxium extends Broadcast, Transaction {

    /**
     * Checks whether the Proxium protocol has been loaded and the port is open.
     *
     * <p>Proxium 프로토콜이 성공적으로 로드되고 포트가 개방되었는지 확인한다.
     *
     * @return whether loaded
     */
    boolean isLoaded();

    /**
     * Returns all platform players connected to the proxy network.
     *
     * <p>프록시 네트워크에 접속해 있는 모든 플랫폼 플레이어 정보를 가져온다.
     *
     * @return immutable map of UUID to proxy player
     */
    @NotNull
    Map<UUID, ProxyPlayer> getPlayers();

    /**
     * Returns the local server name (e.g. {@code "Lobby-1"}).
     *
     * <p>현재 로컬 서버의 이름을 가져온다.
     *
     * @return local server name
     */
    String getName();

    /**
     * Returns a cached server node by its identifier.
     *
     * <p>대상 ID를 통해 캐시된 서버 노드를 가져온다.
     *
     * @param name server's unique identifier
     * @return the server node, or {@code null} if not found
     */
    @Nullable ProxiumNode getNode(String name);

    /**
     * Returns a proxy player by UUID.
     *
     * <p>UUID로 프록시 네트워크의 플레이어를 가져온다.
     *
     * @param uniqueId player UUID
     * @return proxy player, or {@code null} if not found
     */
    @Nullable ProxyPlayer getPlayer(UUID uniqueId);

    /**
     * Sends a transaction request by server name. Internally resolved to {@link ProxiumNode}.
     *
     * <p>대상 서버 이름으로 트랜잭션 요청을 전송한다. 내부적으로 {@link ProxiumNode}로 변환된다.
     *
     * @param target target server name
     * @param channel bridge channel
     * @param request payload to send
     * @param timeout response wait timeout
     * @param <T> request packet type
     * @return RequestContext for chaining response handlers
     * @throws IllegalArgumentException if target server cannot be found
     */
    default <T> RequestContext request(
            String target, BridgeChannel channel, T request, Duration timeout) {
        ProxiumNode node = getNode(target);
        if (node == null) throw new IllegalArgumentException("Unknown server: " + target);
        return request((Node) node, channel, request, timeout);
    }

    /**
     * Sends a transaction request by server name using the default timeout.
     *
     * <p>대상 서버 이름으로 기본 타임아웃으로 트랜잭션 요청을 전송한다.
     *
     * @param target target server name
     * @param channel bridge channel
     * @param request payload to send
     * @param <T> request packet type
     * @return RequestContext for chaining response handlers
     * @throws IllegalArgumentException if target server cannot be found
     */
    default <T> RequestContext request(String target, BridgeChannel channel, T request) {
        return request(target, channel, request, getRequestTimeout());
    }
}
