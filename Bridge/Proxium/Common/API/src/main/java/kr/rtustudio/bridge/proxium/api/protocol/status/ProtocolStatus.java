package kr.rtustudio.bridge.proxium.api.protocol.status;

import kr.rtustudio.bridge.proxium.api.util.ProxiumConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProtocolStatus {

    private final String version = ProxiumConstants.PROXIUM_VERSION;
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
