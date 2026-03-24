package kr.rtustudio.bridge;

import org.jspecify.annotations.NonNull;

/**
 * Base interface for identifying a network node in the bridge system. Each node is uniquely
 * identified by its name.
 *
 * <p>브릿지 시스템에서 네트워크 노드를 식별하는 기본 인터페이스. 각 노드는 이름으로 고유하게 식별된다.
 */
public interface Node {

    /**
     * Returns the unique name of this node.
     *
     * <p>이 노드의 고유 이름을 반환한다.
     *
     * @return node name
     */
    @NonNull String name();
}
