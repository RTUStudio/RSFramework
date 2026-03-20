package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.context.RequestContext;
import kr.rtustudio.bridge.proxium.api.context.ResponseContext;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

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
     * 대상 서버로 단일 RPC 요청(Request)을 전송한다.
     *
     * <p>반환된 {@link RequestContext}의 {@link RequestContext#on(Class,
     * java.util.function.BiConsumer)}을 통해 여러 응답 타입에 대한 핸들러를 체이닝 방식으로 등록할 수 있다. 요청 페이로드 타입은 자동으로 채널에
     * 등록된다.
     *
     * @param target 대상 서버 이름
     * @param channel 브릿지 채널
     * @param request 전송할 페이로드 객체
     * @param timeout 응답 대기 제한 시간
     * @param <T> 요청 패킷 타입
     * @return 응답 핸들러를 등록할 수 있는 RequestContext
     */
    <T> RequestContext request(String target, BridgeChannel channel, T request, Duration timeout);

    /**
     * 대상 서버로 구성에 설정된 기본 타임아웃으로 RPC 요청을 전송한다.
     *
     * @param target 대상 서버 이름
     * @param channel 브릿지 채널
     * @param request 전송할 페이로드 객체
     * @param <T> 요청 패킷 타입
     * @return 응답 핸들러를 등록할 수 있는 RequestContext
     */
    default <T> RequestContext request(String target, BridgeChannel channel, T request) {
        return request(target, channel, request, getRequestTimeout());
    }

    /**
     * RPC 요청의 기본 타임아웃 시간을 반환한다.
     *
     * @return 구성에서 설정된 기본 타임아웃
     */
    Duration getRequestTimeout();

    /**
     * 특정 채널에 대한 응답 핸들러 등록기를 반환한다.
     *
     * <p>반환된 {@link ResponseContext}의 {@link ResponseContext#on(Class,
     * kr.rtustudio.bridge.proxium.api.handler.ResponseHandler)}을 통해 여러 요청 타입에 대한 핸들러를 체이닝 방식으로 등록할
     * 수 있다. 등록된 요청 타입은 자동으로 채널에 등록된다.
     *
     * @param channel 브릿지 채널
     * @return 핸들러 등록기
     */
    ResponseContext respond(BridgeChannel channel);
}
