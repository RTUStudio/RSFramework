package kr.rtuserver.protoweaver.api.proxy;

import kr.rtuserver.protoweaver.api.client.ProtoClient;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import lombok.Getter;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProtoServer {

    @Getter
    private final String name;
    @Getter
    private final SocketAddress address;
    private List<ProtoClient> clients = new ArrayList<>();

    public ProtoServer(String name, SocketAddress address) {
        this.name = name;
        this.address = address;
    }

    public boolean isConnected(Protocol protocol) {
        for (ProtoClient client : clients) {
            if (client.isConnected() && client.getCurrentProtocol().equals(protocol)) return true;
        }
        return false;
    }

    public Optional<ProtoConnection> getConnection(Protocol protocol) {
        for (ProtoClient client : clients) {
            if (client.isConnected() && client.getCurrentProtocol().equals(protocol))
                return Optional.ofNullable(client.getConnection());
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProtoServer server && Objects.equals(server.name, name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + " : " + address;
    }
}