package kr.rtuserver.protoweaver.core.protocol.protoweaver;

import kr.rtuserver.protoweaver.api.util.ProtoConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ProtocolStatus {

    private final String protoweaverVersion = ProtoConstants.PROTOWEAVER_VERSION;
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