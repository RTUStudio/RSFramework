package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.ProxiumPipeline;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import lombok.Setter;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * 내부 시스템에서만 사용하는 변경 가능한 ProxyPlayer.
 *
 * <p>외부 플러그인은 {@link ProxyPlayer}(불변 뷰)만 접근 가능합니다.
 */
public class MutableProxyPlayer extends ProxyPlayer {

    /** 서버 노드를 갱신한다. 내부 시스템 전용. */
    @Setter @Nullable private ProxiumNode node;

    public MutableProxyPlayer(
            ProxiumPipeline proxium, UUID uniqueId, String name, @Nullable ProxiumNode node) {
        super(proxium, uniqueId, name, node);
    }

    @Override
    @Nullable
    public ProxiumNode getNode() {
        return node;
    }

    @Override
    @Nullable
    public String getServer() {
        return node != null ? node.name() : null;
    }
}
