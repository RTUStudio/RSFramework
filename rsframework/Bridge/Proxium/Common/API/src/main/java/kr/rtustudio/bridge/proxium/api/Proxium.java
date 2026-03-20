package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/** 프록시 노드와 단일 지점(Point-to-Point) Netty 커넥션을 맺고 통신하는 Proxium 브릿지 인터페이스. */
public interface Proxium extends Bridge {

    /**
     * Proxium 프로토콜이 성공적으로 로드되고 포트가 개방되었는지 확인한다.
     *
     * @return 로드 완료 상태
     */
    boolean isLoaded();

    /**
     * 프록시 네트워크에 접속해 있는 모든 플랫폼 플레이어 정보를 가져온다.
     *
     * @return 고유 UUID와 프록시 플레이어 객체를 매핑한 불변 맵(Immutable Map)
     */
    @NotNull
    Map<UUID, ProxyPlayer> getPlayers();

    /**
     * 현재 로컬 서버의 이름을 가져온다. (예: Lobby-1)
     *
     * @return 로컬 서버 명칭
     */
    String getServer();

    /**
     * 대상 ID를 통해 캐시된 서버 노드를 가져온다.
     *
     * @param name 대상 서버의 고유 식별자
     * @return 대상 노드를 나타내는 ProxiumNode, 없으면 null
     */
    @Nullable ProxiumNode getServer(String name);

    /**
     * UUID로 프록시 네트워크의 플레이어를 가져온다.
     *
     * @param uniqueId 플레이어 UUID
     * @return 프록시 플레이어, 없으면 null
     */
    @Nullable ProxyPlayer getPlayer(UUID uniqueId);

    /**
     * 대상 노드로 단일 RPC 요청(Request)을 전송한다.
     *
     * @param target 대상 노드
     * @param channel 브릿지 채널
     * @param request 전송할 페이로드 객체
     * @param timeout 응답 대기 제한 시간
     * @param <T> 요청 패킷 타입
     * @param <R> 응답 패킷 타입
     * @return 응답을 받을 수 있는 비동기 Future
     */
    <T, R> CompletableFuture<R> request(
            ProxiumNode target, BridgeChannel channel, T request, Duration timeout);

    /**
     * 응답 타입을 명시하여 타입 안전한 RPC 요청을 전송한다.
     *
     * <p>제네릭 파라미터를 직접 명시하지 않아도 응답 타입이 자동으로 추론된다.
     *
     * @param target 대상 노드
     * @param channel 브릿지 채널
     * @param request 전송할 페이로드 객체
     * @param responseType 기대하는 응답 타입 클래스
     * @param timeout 응답 대기 제한 시간
     * @param <T> 요청 패킷 타입
     * @param <R> 응답 패킷 타입
     * @return 응답을 받을 수 있는 비동기 Future
     */
    default <T, R> CompletableFuture<R> request(
            ProxiumNode target,
            BridgeChannel channel,
            T request,
            Class<R> responseType,
            Duration timeout) {
        register(channel, request.getClass(), responseType);
        return this.<T, R>request(target, channel, request, timeout).thenApply(responseType::cast);
    }

    /**
     * 특정 채널로 수신되는 1:1 요청에 대한 응답 핸들러(ResponseHandler)를 등록한다.
     *
     * @param channel 브릿지 채널
     * @param handler 요청을 받아 처리할 콜백 핸들러
     * @param <T> 수신할 요청 객체의 타입
     * @param <R> 반환할 응답 객체의 타입
     */
    <T, R> void respond(BridgeChannel channel, ResponseHandler<T, R> handler);

    /**
     * 요청 타입을 명시하여 타입 안전한 응답 핸들러를 등록한다.
     *
     * <p>같은 채널에 여러 타입의 핸들러를 등록할 수 있으며, 요청 payload 타입으로 자동 디스패치된다.
     *
     * @param channel 브릿지 채널
     * @param type 수신할 요청 객체의 타입 클래스
     * @param handler 요청을 받아 처리할 콜백 핸들러
     * @param <T> 수신할 요청 객체의 타입
     * @param <R> 반환할 응답 객체의 타입
     */
    <T, R> void respond(BridgeChannel channel, Class<T> type, ResponseHandler<T, R> handler);
}
