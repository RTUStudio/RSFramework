package kr.rtuserver.protoweaver.api.callback;

import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HandlerCallback {

    private final Callback<Ready> readyCall;
    private final Callback<Packet> packetCall;

    public void onReady(ProtoConnection connection) {
        if (readyCall != null) readyCall.run(new Ready(connection));
    }

    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (packetCall != null) packetCall.run(new Packet(protoConnection, packet));
    }

    public interface Callback<T> {
        void run(T data);
    }

    public record Ready(ProtoConnection protoConnection) {
    }

    public record Packet(ProtoConnection protoConnection, Object packet) {
    }

}
