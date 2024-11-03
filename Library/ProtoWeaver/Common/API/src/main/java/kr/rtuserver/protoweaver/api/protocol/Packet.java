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
    private final boolean global;
    private final boolean isBothSide;

    /**
     * @param type       패킷의 타입
     * @param global     다른 모든 서버에 전송할지 (기본값: true)
     * @param isBothSide 프록시와 서버 모두 등록되는 패킷인지 (기본값: false)
     */
    public static Packet of(Class<?> type) {
        return new Packet(type.getName(), true, false);
    }

    /**
     * @param type       패킷의 타입
     * @param global     다른 모든 서버에 전송할지 (기본값: true)
     * @param isBothSide 프록시와 서버 모두 등록되는 패킷인지 (기본값: false)
     */
    public static Packet of(Class<?> type, boolean global) {
        return new Packet(type.getName(), global, false);
    }

    /**
     * @param type       패킷의 타입
     * @param global     다른 모든 서버에 전송할지 (기본값: true)
     * @param isBothSide 프록시와 서버 모두 등록되는 패킷인지 (기본값: false)
     */
    public static Packet of(Class<?> type, boolean global, boolean isBothSide) {
        return new Packet(type.getName(), global, isBothSide);
    }

    public Class<?> getTypeClass() {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
