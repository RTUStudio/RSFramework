package kr.rtuserver.protoweaver.api.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Packet {

    private final String type;
    private final boolean isBothSide;

    /**
     * @param type 패킷의 타입
     */
    public static Packet of(Class<?> type) {
        return new Packet(type.getName(), false);
    }

    /**
     * @param type       패킷의 타입
     * @param isBothSide 패킷의 옵션 (기본값: false)
     */
    public static Packet of(Class<?> type, boolean isBothSide) {
        return new Packet(type.getName(), isBothSide);
    }

    public Class<?> getTypeClass() {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
