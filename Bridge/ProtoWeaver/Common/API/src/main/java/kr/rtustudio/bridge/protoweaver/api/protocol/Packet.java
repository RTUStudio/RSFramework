package kr.rtustudio.bridge.protoweaver.api.protocol;

import kr.rtustudio.bridge.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtustudio.bridge.protoweaver.api.serializer.ProtoSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Packet implements InternalPacket {

    private final Class<?> packet;
    private final Class<? extends ProtoSerializer<?>> serializer;

    /**
     * @param packet 패킷 클래스
     */
    public static Packet of(Class<?> packet) {
        return new Packet(packet, null);
    }

    /**
     * @param packet 패킷 클래스
     * @param serializer 직렬화 클래스
     */
    public static Packet of(Class<?> packet, Class<? extends ProtoSerializer<?>> serializer) {
        return new Packet(packet, serializer);
    }
}
