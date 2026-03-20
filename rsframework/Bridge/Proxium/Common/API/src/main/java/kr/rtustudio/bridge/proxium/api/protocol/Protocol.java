package kr.rtustudio.bridge.proxium.api.protocol;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ConnectionHandler;
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ProxyAuthHandler;
import kr.rtustudio.bridge.proxium.api.protocol.handler.auth.ServerAuthHandler;
import lombok.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.fory.Fory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.logging.LoggerFactory;

/**
 * Stores all the registered packets, settings and additional configuration of a {@link ProxiumAPI}
 * protocol.
 */
@EqualsAndHashCode
public class Protocol {

    private static final Logger LOGGER = Logger.getLogger(Protocol.class.getName());

    static {
        LoggerFactory.disableLogging();
    }

    @EqualsAndHashCode.Exclude
    private final Fory serializer =
            Fory.builder()
                    .withJdkClassSerializableCheck(false)
                    .withDeserializeUnknownClass(false)
                    .withLanguage(Language.JAVA)
                    .withCompatibleMode(CompatibleMode.COMPATIBLE)
                    .withAsyncCompilation(true)
                    .withClassLoader(Protocol.class.getClassLoader())
                    .build();

    @Getter private final MessageDigest packetMD = MessageDigest.getInstance("SHA-1");
    @Getter private final BridgeChannel channel;
    @Getter private CompressionType compression = CompressionType.NONE;
    @Getter private int compressionLevel = -37;
    @Getter private int maxPacketSize = 16384;
    @Getter private int maxConnections = -1;
    @Getter private Level loggingLevel = Level.ALL;
    @Getter @EqualsAndHashCode.Exclude private BridgeOptions options;

    @EqualsAndHashCode.Exclude private Supplier<? extends ConnectionHandler> serverHandlerFactory;

    @EqualsAndHashCode.Exclude
    private Constructor<? extends ConnectionHandler> serverConnectionHandler;

    @EqualsAndHashCode.Exclude private Object[] serverConnectionHandlerArgs = new Object[0];

    @EqualsAndHashCode.Exclude private Supplier<? extends ConnectionHandler> proxyHandlerFactory;

    @EqualsAndHashCode.Exclude
    private Constructor<? extends ConnectionHandler> proxyConnectionHandler;

    @EqualsAndHashCode.Exclude private Object[] proxyConnectionHandlerArgs = new Object[0];

    @EqualsAndHashCode.Exclude private Supplier<? extends ServerAuthHandler> serverAuthFactory;
    @EqualsAndHashCode.Exclude private Constructor<? extends ServerAuthHandler> serverAuthHandler;
    @EqualsAndHashCode.Exclude private Object[] serverAuthHandlerArgs = new Object[0];

    @EqualsAndHashCode.Exclude private Supplier<? extends ProxyAuthHandler> proxyAuthFactory;
    @EqualsAndHashCode.Exclude private Constructor<? extends ProxyAuthHandler> proxyAuthHandler;
    @EqualsAndHashCode.Exclude private Object[] proxyAuthHandlerArgs = new Object[0];

    private Protocol(BridgeChannel channel) throws NoSuchAlgorithmException {
        this.channel = channel;
    }

    /**
     * Creates a new protocol builder. A good rule of thumb for naming that ensures maximum
     * compatibility is to use your mod id or project id for the namespace and to give the name
     * something unique. <br>
     * For example: "proxium:message"</br>
     *
     * @param channel The BridgeChannel of your protocol.
     */
    @SneakyThrows
    public static Builder create(@NonNull BridgeChannel channel) {
        return new Builder(new Protocol(channel));
    }

    /**
     * Allows you to create modify an existing {@link Protocol}. The {@link Protocol} object
     * returned from {@link Builder#build()} will be the same object as the one that this method was
     * called on (not a copy). In theory this means you can modify a protocol without reloading it,
     * or while its currently active. Here be dragons, so use with caution.
     */
    public Builder modify() {
        return new Builder(this);
    }

    @SneakyThrows
    public ConnectionHandler newConnectionHandler(Side side) {
        return switch (side) {
            case PROXY -> {
                if (proxyHandlerFactory != null) yield proxyHandlerFactory.get();
                if (proxyConnectionHandler == null)
                    throw new RuntimeException(
                            "No proxy connection handler set for protocol: " + this);
                yield proxyConnectionHandler.newInstance(proxyConnectionHandlerArgs);
            }
            case SERVER -> {
                if (serverHandlerFactory != null) yield serverHandlerFactory.get();
                if (serverConnectionHandler == null)
                    throw new RuntimeException(
                            "No server connection handler set for protocol: " + this);
                yield serverConnectionHandler.newInstance(serverConnectionHandlerArgs);
            }
        };
    }

    @SneakyThrows
    public ServerAuthHandler newServerAuthHandler() {
        if (serverAuthFactory != null) return serverAuthFactory.get();
        if (serverAuthHandler == null)
            throw new RuntimeException("No server auth handler set for protocol: " + this);
        return serverAuthHandler.newInstance(serverAuthHandlerArgs);
    }

    @SneakyThrows
    public ProxyAuthHandler newProxyAuthHandler() {
        if (proxyAuthFactory != null) return proxyAuthFactory.get();
        if (proxyAuthHandler == null)
            throw new RuntimeException("No proxy auth handler set for protocol: " + this);
        return proxyAuthHandler.newInstance(proxyAuthHandlerArgs);
    }

    private void registerPacketType(@NonNull Class<?> type) {
        registerPacketTypeRecursive(type, new HashSet<>());
    }

    private void registerPacketTypeRecursive(Class<?> type, Set<Class<?>> registered) {
        if (type == null
                || type == Object.class
                || registered.contains(type)
                || Modifier.isAbstract(type.getModifiers())) {
            return;
        }

        synchronized (serializer) {
            serializer.register(type);
        }
        registered.add(type);

        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            registerPacketTypeRecursive(field.getType(), registered);
        }

        Type genericSuperclass = type.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            for (Type genericType : parameterizedType.getActualTypeArguments()) {
                try {
                    registerPacketTypeRecursive(
                            Class.forName(genericType.getTypeName()), registered);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        if (!type.isEnum()) {
            registerPacketTypeRecursive(type.getSuperclass(), registered);
        }
    }

    public byte[] serialize(@NonNull Object packet) throws IllegalArgumentException {
        synchronized (serializer) {
            try {
                return serializer.serialize(packet);
            } catch (InsecureException e) {
                throw new IllegalArgumentException(
                        "unregistered object: " + packet.getClass().getName(), e);
            }
        }
    }

    public Object deserialize(byte @NonNull [] packet) throws IllegalArgumentException {
        synchronized (serializer) {
            try {
                return serializer.deserialize(packet);
            } catch (InsecureException e) {
                String packetName =
                        e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("unregistered object: " + packetName, e);
            }
        }
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
     * @return The number of connected proxies this protocol is currently serving.
     */
    public int getConnections() {
        return Connection.getConnectionCount(this);
    }

    /**
     * Determine if a side requires auth by checking to see if an auth handler was set for the given
     * side.
     *
     * @param side The {@link Side} to check for an auth handler.
     */
    public boolean requiresAuth(@NonNull Side side) {
        if (side.isProxy()) return proxyAuthFactory != null || proxyAuthHandler != null;
        return serverAuthFactory != null || serverAuthHandler != null;
    }

    public void logInfo(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.INFO.intValue()) {
            LOGGER.log(Level.INFO, "[{0}] {1}", new Object[] {this, message});
        }
    }

    public void logWarn(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.WARNING.intValue()) {
            LOGGER.log(Level.WARNING, "[{0}] {1}", new Object[] {this, message});
        }
    }

    public void logErr(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.SEVERE.intValue()) {
            LOGGER.log(Level.SEVERE, "[{0}] {1}", new Object[] {this, message});
        }
    }

    @Override
    public String toString() {
        return channel.toString();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final Protocol protocol;

        private final Set<Class<?>> packets = new HashSet<>();

        private Class<?>[] getArgTypes(Object[] args) {
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();
            return types;
        }

        /**
         * Set the BridgeOptions for frame encoding/decoding.
         *
         * @param options The BridgeOptions to use.
         */
        public Builder setOptions(@NonNull BridgeOptions options) {
            protocol.options = options;
            return this;
        }

        /**
         * Set the packets handler that the server will use to process inbound packets.
         *
         * @param factory Supplier that creates a new handler instance per connection.
         */
        public Builder setServerHandler(@NonNull Supplier<? extends ConnectionHandler> factory) {
            protocol.serverHandlerFactory = factory;
            return this;
        }

        /**
         * Set the packets handler that the server will use to process inbound packets.
         *
         * @param handler The class of the packets handler.
         */
        @SneakyThrows
        public Builder setServerHandler(
                Class<? extends ConnectionHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.serverConnectionHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.serverConnectionHandlerArgs = args;
            return this;
        }

        /**
         * Set the packets handler that the proxy will use to process inbound packets.
         *
         * @param factory Supplier that creates a new handler instance per connection.
         */
        public Builder setProxyHandler(@NonNull Supplier<? extends ConnectionHandler> factory) {
            protocol.proxyHandlerFactory = factory;
            return this;
        }

        /**
         * Set the packets handler that the proxy will use to process inbound packets.
         *
         * @param handler The class of the packets handler.
         */
        @SneakyThrows
        public Builder setProxyHandler(Class<? extends ConnectionHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.proxyConnectionHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.proxyConnectionHandlerArgs = args;
            return this;
        }

        /**
         * Set the auth handler that the server will use to process inbound proxy secrets.
         *
         * @param factory Supplier that creates a new auth handler instance.
         */
        public Builder setServerAuthHandler(
                @NonNull Supplier<? extends ServerAuthHandler> factory) {
            protocol.serverAuthFactory = factory;
            return this;
        }

        /**
         * Set the auth handler that the server will use to process inbound proxy secrets.
         *
         * @param handler The class of the auth handler.
         */
        @SneakyThrows
        public Builder setServerAuthHandler(
                Class<? extends ServerAuthHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.serverAuthHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.serverAuthHandlerArgs = args;
            return this;
        }

        /**
         * Set the auth handler that the proxy will use to get the secret that will be sent to the
         * server.
         *
         * @param factory Supplier that creates a new auth handler instance.
         */
        public Builder setProxyAuthHandler(@NonNull Supplier<? extends ProxyAuthHandler> factory) {
            protocol.proxyAuthFactory = factory;
            return this;
        }

        /**
         * Set the auth handler that the proxy will use to get the secret that will be sent to the
         * server.
         *
         * @param handler The class of the auth handler.
         */
        @SneakyThrows
        public Builder setProxyAuthHandler(
                Class<? extends ProxyAuthHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.proxyAuthHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.proxyAuthHandlerArgs = args;
            return this;
        }

        /**
         * Register a class to the {@link Protocol}. Does nothing if the class has already been
         * registered.
         *
         * @param packet The packets to register.
         */
        public Builder addPacket(@NonNull Class<?> packet) {
            protocol.registerPacketType(packet);
            if (!packets.contains(packet)) {
                protocol.packetMD.update(packet.getName().getBytes(StandardCharsets.UTF_8));
                packets.add(packet);
            }
            return this;
        }

        /**
         * Enables compression on the {@link Protocol}. The compression type by defaults is set to
         * {@link CompressionType#NONE}.
         *
         * @param type The type of compression to enable.
         */
        public Builder setCompression(@NonNull CompressionType type) {
            protocol.compression = type;
            return this;
        }

        /**
         * Set the compression level if compression is enabled. Be sure to check the supported level
         * for each type of compression online.
         *
         * @param level The compression level to set.
         */
        public Builder setCompressionLevel(int level) {
            protocol.compressionLevel = level;
            return this;
        }

        /**
         * Set the maximum packets size this {@link Protocol} can handle. The higher the value, the
         * more ram will be allocated when sending and receiving packets. The maximum packets size
         * defaults to 16kb.
         *
         * @param maxPacketSize The maximum size a packets can be in bytes.
         */
        public Builder setMaxPacketSize(int maxPacketSize) {
            protocol.maxPacketSize = maxPacketSize;
            return this;
        }

        /**
         * Set the number of maximum concurrent connections this {@link Protocol} will allow. Any
         * connections over this limit will be disconnected. The maximum connections defaults to -1
         * and allows any number of connections.
         *
         * @param maxConnections The maximum concurrent connections.
         */
        public Builder setMaxConnections(int maxConnections) {
            protocol.maxConnections = maxConnections;
            return this;
        }

        /** Sets the logging level for this {@link Protocol}. */
        public Builder setLoggingLevel(Level level) {
            protocol.loggingLevel = level;
            return this;
        }

        /**
         * Build the {@link Protocol}.
         *
         * @return A finished protocol that can be loaded using {@link ProxiumAPI#load(Protocol)}.
         */
        public Protocol build() {
            if (protocol.compression != CompressionType.NONE && protocol.compressionLevel == -37)
                protocol.compressionLevel = protocol.compression.getDefaultLevel();
            return protocol;
        }

        /**
         * Equivalent to calling {@link Builder#build()} and {@link ProxiumAPI#load(Protocol)}.
         *
         * @return The {@link Protocol} that was built and loaded.
         */
        public Protocol load() {
            ProxiumAPI.load(build());
            return protocol;
        }
    }
}
