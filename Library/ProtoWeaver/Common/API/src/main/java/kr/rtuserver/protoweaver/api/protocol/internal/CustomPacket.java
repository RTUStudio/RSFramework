package kr.rtuserver.protoweaver.api.protocol.internal;


public record CustomPacket(String classType, String handlerClass, String json) implements InternalPacket {
}
