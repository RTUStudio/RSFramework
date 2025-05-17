package kr.rtuserver.protoweaver.api.protocol.internal;

// 프록시에 등록되지 않은 커스텀 패킷의 서버 간 전달을 위한 클래스입니다
public record CustomPacket(String classType, String handlerClass, String json) implements GlobalPacket {
}