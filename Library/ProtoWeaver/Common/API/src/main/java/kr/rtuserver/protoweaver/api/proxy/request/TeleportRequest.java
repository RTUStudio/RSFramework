package kr.rtuserver.protoweaver.api.proxy.request;

import kr.rtuserver.protoweaver.api.protocol.internal.InternalPacket;
import kr.rtuserver.protoweaver.api.proxy.ProxyLocation;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;

public interface TeleportRequest extends Request, InternalPacket {

    ProxyPlayer player();

    String server();

    record Location(ProxyPlayer player, ProxyLocation location) implements TeleportRequest {

        public String server() {
            return location.server();
        }

    }

    record Player(ProxyPlayer player, ProxyPlayer location) implements TeleportRequest {

        public String server() {
            return location.getServer();
        }

    }

}

