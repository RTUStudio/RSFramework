package kr.rtustudio.bridge.proxium.api.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Packet {

    private final Class<?> packet;

    /**
     * @param packet 패킷 클래스
     */
    public static Packet of(Class<?> packet) {
        return new Packet(packet);
    }
}
