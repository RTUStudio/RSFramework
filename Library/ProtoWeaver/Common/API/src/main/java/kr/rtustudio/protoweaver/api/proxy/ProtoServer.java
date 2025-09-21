package kr.rtustudio.protoweaver.api.proxy;

import lombok.Getter;

import java.net.SocketAddress;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public record ProtoServer(@Getter String name, @Getter SocketAddress address) {

    // private List<ProtoClient> clients = new ArrayList<>();
    //
    // public boolean isConnected(Protocol protocol) {
    // for (ProtoClient client : clients) {
    // if (client.isConnected() && client.getCurrentProtocol().equals(protocol))
    // return
    // true;
    // }
    // return false;
    // }
    //
    // public Optional<ProtoConnection> getConnection(Protocol protocol) {
    // for (ProtoClient client : clients) {
    // if (client.isConnected() && client.getCurrentProtocol().equals(protocol))
    // return Optional.ofNullable(client.getConnection());
    // }
    // return Optional.empty();
    // }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProtoServer server && Objects.equals(server.name, name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return name + " : " + address;
    }
}
