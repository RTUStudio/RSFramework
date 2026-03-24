package kr.rtustudio.bridge.proxium.api.proxy;

import kr.rtustudio.bridge.proxium.api.ProxiumNode;

/**
 * 크로스 서버 위치 정보를 나타내는 레코드.
 *
 * <p>서버, 월드, 좌표(x/y/z), 시점(yaw/pitch)을 포함한다.
 *
 * @param server 대상 서버 노드
 * @param world 월드 이름
 * @param x X 좌표
 * @param y Y 좌표
 * @param z Z 좌표
 * @param yaw 수평 시점 각도
 * @param pitch 수직 시점 각도
 */
public record ProxyLocation(
        ProxiumNode server, String world, double x, double y, double z, float yaw, float pitch) {

    /** yaw/pitch를 생략하는 편의 생성자. 기본값 0으로 설정된다. */
    public ProxyLocation(ProxiumNode server, String world, double x, double y, double z) {
        this(server, world, x, y, z, 0, 0);
    }

    public ProxiumNode getNode() {
        return server();
    }
}
