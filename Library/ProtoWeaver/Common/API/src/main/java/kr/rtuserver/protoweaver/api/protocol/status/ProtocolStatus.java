package kr.rtuserver.protoweaver.api.protocol.status;

import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtuserver.protoweaver.api.util.ProtoConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProtocolStatus implements InternalPacket {

    private final String version = ProtoConstants.PROTOWEAVER_VERSION;
    private String currentProtocol;
    private String nextProtocol;
    private byte[] nextSHA1;
    private Status status;

    public enum Status {
        MISSING,
        MISMATCH,
        FULL,
        START,
        UPGRADE
    }
}
