package kr.rtustudio.bridge;

import net.kyori.adventure.key.Key;

import org.jetbrains.annotations.NotNull;

/**
 * Identifies a message channel using a namespace and key pair. Predefined channels include {@link
 * #INTERNAL}, {@link #PROXIUM}, and {@link #AUDIENCE}.
 *
 * <p>네임스페이스와 키 쌍으로 메시지 채널을 식별하는 레코드. {@link #INTERNAL}, {@link #PROXIUM}, {@link #AUDIENCE} 등 미리
 * 정의된 채널을 포함한다.
 *
 * @param namespace channel namespace
 * @param key channel key
 */
public record BridgeChannel(@NotNull String namespace, @NotNull String key) {

    public static final BridgeChannel INTERNAL = new BridgeChannel("rsframework", "internal");
    public static final BridgeChannel PROXIUM = new BridgeChannel("rsframework", "proxium");
    public static final BridgeChannel AUDIENCE = new BridgeChannel("rsframework", "audience");

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
