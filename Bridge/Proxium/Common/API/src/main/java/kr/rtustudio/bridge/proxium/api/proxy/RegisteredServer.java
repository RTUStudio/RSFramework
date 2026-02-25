package kr.rtustudio.bridge.proxium.api.proxy;

import lombok.Getter;

import java.net.SocketAddress;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public record RegisteredServer(@Getter String name, @Getter SocketAddress address) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RegisteredServer server && Objects.equals(server.name, name);
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
