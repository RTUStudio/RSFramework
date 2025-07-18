package kr.rtuserver.protoweaver.api.proxy;

import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Getter
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProxyPlayer implements InternalPacket {

    private final UUID uniqueId;
    private String server;
    private String name;

}
