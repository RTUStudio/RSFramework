package kr.rtustudio.bridge.proxium.api.protocol;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.handler.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ProxyAuthHandler;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ServerAuthHandler;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Per-channel connection profile managing compression, handler factories, and authentication.
 * Separated from BridgeOptions (serialization); multiple Protocols may share one BridgeOptions.
 *
 * <p>채널별 커넥션 프로필. 압축, 핸들러 팩토리, 인증 등 채널 단위 설정을 묶어 관리한다. BridgeOptions(직렬화)와 분리되어, 하나의 BridgeOptions에
 * 여러 Protocol이 대응된다.
 */
@Slf4j(topic = "Proxium")
@EqualsAndHashCode
public class Protocol {

    @Getter private final MessageDigest packetMD = MessageDigest.getInstance("SHA-1");
    @Getter private final BridgeChannel channel;
    @Getter private CompressionType compression = CompressionType.NONE;
    @Getter private int compressionLevel = -37;
    @Getter private int maxPacketSize = 16384;
    @Getter private int maxConnections = -1;
    @Getter @EqualsAndHashCode.Exclude private BridgeOptions options;

    @EqualsAndHashCode.Exclude private Supplier<? extends ConnectionHandler> serverHandlerFactory;
    @EqualsAndHashCode.Exclude private Supplier<? extends ConnectionHandler> proxyHandlerFactory;
    @EqualsAndHashCode.Exclude private Supplier<? extends ServerAuthHandler> serverAuthFactory;
    @EqualsAndHashCode.Exclude private Supplier<? extends ProxyAuthHandler> proxyAuthFactory;

    private Protocol(BridgeChannel channel) throws NoSuchAlgorithmException {
        this.channel = channel;
    }

    /**
     * Creates a new Protocol Builder.
     *
     * <p>새 Protocol Builder를 생성한다.
     *
     * @param channel the BridgeChannel (e.g. {@code BridgeChannel.INTERNAL})
     */
    @SneakyThrows
    public static Builder create(@NonNull BridgeChannel channel) {
        return new Builder(new Protocol(channel));
    }

    @SneakyThrows
    public ConnectionHandler newConnectionHandler(Side side) {
        Supplier<? extends ConnectionHandler> factory =
                side.isProxy() ? proxyHandlerFactory : serverHandlerFactory;
        if (factory == null)
            throw new IllegalStateException(
                    side + " connection handler not set for protocol: " + this);
        return factory.get();
    }

    @SneakyThrows
    public ServerAuthHandler newServerAuthHandler() {
        if (serverAuthFactory == null)
            throw new IllegalStateException("Server auth handler not set for protocol: " + this);
        return serverAuthFactory.get();
    }

    @SneakyThrows
    public ProxyAuthHandler newProxyAuthHandler() {
        if (proxyAuthFactory == null)
            throw new IllegalStateException("Proxy auth handler not set for protocol: " + this);
        return proxyAuthFactory.get();
    }

    @SneakyThrows
    public byte[] getSHA1() {
        MessageDigest md = (MessageDigest) this.packetMD.clone();
        md.update(toString().getBytes(StandardCharsets.UTF_8));
        md.update(
                ByteBuffer.allocate(12)
                        .putInt(compressionLevel)
                        .putInt(compression.ordinal())
                        .putInt(maxPacketSize)
                        .array());
        return md.digest();
    }

    /**
     * Returns the current connection count for this protocol.
     *
     * <p>현재 이 Protocol에 연결된 커넥션 수를 반환한다.
     */
    public int getConnections() {
        return Connection.getConnectionCount(this);
    }

    /**
     * Checks whether authentication is required for the given side.
     *
     * <p>지정한 Side에 대해 인증이 필요한지 확인한다.
     *
     * @param side the {@link Side} to check
     */
    public boolean requiresAuth(@NonNull Side side) {
        return side.isProxy() ? proxyAuthFactory != null : serverAuthFactory != null;
    }

    @Override
    public String toString() {
        return channel.toString();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final Protocol protocol;
        private final Set<Class<?>> packets = new HashSet<>();

        /**
         * Sets the BridgeOptions for serialization.
         *
         * <p>직렬화에 사용할 BridgeOptions를 설정한다.
         */
        public Builder setOptions(@NonNull BridgeOptions options) {
            protocol.options = options;
            return this;
        }

        /**
         * Sets the server-side connection handler factory.
         *
         * <p>서버측 커넥션 핸들러 팩토리를 설정한다.
         */
        public Builder setServerHandler(@NonNull Supplier<? extends ConnectionHandler> factory) {
            protocol.serverHandlerFactory = factory;
            return this;
        }

        /**
         * Sets the proxy-side connection handler factory.
         *
         * <p>프록시측 커넥션 핸들러 팩토리를 설정한다.
         */
        public Builder setProxyHandler(@NonNull Supplier<? extends ConnectionHandler> factory) {
            protocol.proxyHandlerFactory = factory;
            return this;
        }

        /**
         * Sets the server-side auth handler factory.
         *
         * <p>서버측 인증 핸들러 팩토리를 설정한다.
         */
        public Builder setServerAuthHandler(
                @NonNull Supplier<? extends ServerAuthHandler> factory) {
            protocol.serverAuthFactory = factory;
            return this;
        }

        /**
         * Sets the proxy-side auth handler factory.
         *
         * <p>프록시측 인증 핸들러 팩토리를 설정한다.
         */
        public Builder setProxyAuthHandler(@NonNull Supplier<? extends ProxyAuthHandler> factory) {
            protocol.proxyAuthFactory = factory;
            return this;
        }

        /**
         * Registers packet classes for SHA1 hashing. Also registers with BridgeOptions.
         *
         * <p>패킷 클래스를 SHA1 해시에 등록한다.
         */
        public Builder addPacket(@NonNull Class<?> packet) {
            if (protocol.options != null) {
                protocol.options.register(protocol.channel, packet);
            }
            if (!packets.contains(packet)) {
                protocol.packetMD.update(packet.getName().getBytes(StandardCharsets.UTF_8));
                packets.add(packet);
            }
            return this;
        }

        /**
         * Sets the compression type. Default: {@link CompressionType#NONE}.
         *
         * <p>압축 타입을 설정한다.
         */
        public Builder setCompression(@NonNull CompressionType type) {
            protocol.compression = type;
            return this;
        }

        /**
         * Sets the compression level.
         *
         * <p>압축 레벨을 설정한다.
         */
        public Builder setCompressionLevel(int level) {
            protocol.compressionLevel = level;
            return this;
        }

        /**
         * Sets the max packet size in bytes. Default: 16384 (16KB).
         *
         * <p>최대 패킷 크기를 설정한다.
         */
        public Builder setMaxPacketSize(int maxPacketSize) {
            protocol.maxPacketSize = maxPacketSize;
            return this;
        }

        /**
         * Sets the max concurrent connections. Default: -1 (unlimited).
         *
         * <p>최대 동시 접속 수를 설정한다.
         */
        public Builder setMaxConnections(int maxConnections) {
            protocol.maxConnections = maxConnections;
            return this;
        }

        /**
         * Builds the Protocol.
         *
         * <p>Protocol을 빌드한다.
         */
        public Protocol build() {
            if (protocol.compression != CompressionType.NONE && protocol.compressionLevel == -37)
                protocol.compressionLevel = protocol.compression.getDefaultLevel();
            return protocol;
        }

        /**
         * Builds the Protocol and loads it into {@link ProxiumAPI}.
         *
         * <p>Protocol을 빌드하고 {@link ProxiumAPI}에 로드한다.
         *
         * @return built and loaded Protocol
         */
        public Protocol load() {
            ProxiumAPI.load(build());
            return protocol;
        }
    }
}
