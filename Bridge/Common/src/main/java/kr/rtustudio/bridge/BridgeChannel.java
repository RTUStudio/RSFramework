package kr.rtustudio.bridge;

import net.kyori.adventure.key.Key;

import org.jetbrains.annotations.NotNull;

public record BridgeChannel(@NotNull String namespace, @NotNull String key) {

    public static final BridgeChannel INTERNAL = new BridgeChannel("rsframework", "internal");
    public static final BridgeChannel PROXIUM = new BridgeChannel("rsframework", "proxium");

    public static BridgeChannel of(@NotNull String channel) {
        String[] parts = channel.split(":", 2);
        String namespace = parts[0];
        String key = parts.length > 1 ? parts[1] : parts[0];
        return new BridgeChannel(namespace, key);
    }

    public static BridgeChannel of(@NotNull String namespace, @NotNull String key) {
        return new BridgeChannel(namespace, key);
    }

    public boolean isInternal() {
        return this.equals(INTERNAL);
    }

    public boolean isProxium() {
        return this.equals(PROXIUM);
    }

    @NotNull
    public Key toKey() {
        return Key.key(namespace, key);
    }

    @Override
    @NotNull
    public String toString() {
        return namespace + ":" + key;
    }
}
