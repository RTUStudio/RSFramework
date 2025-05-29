package kr.rtuserver.protoweaver.api.protocol.internal;

import com.google.gson.Gson;

// 프록시에 등록되지 않은 커스텀 패킷의 서버 간 전달을 위한 클래스입니다
public record CustomPacket(String classType, String json) implements GlobalPacket {

    private static final Gson GSON = new Gson();

    public CustomPacket(Object packet) {
        this(packet.getClass().getName(), GSON.toJson(packet));
    }

}