package kr.rtuserver.protoweaver.api.protocol;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.ProtoSerializer;
import kr.rtuserver.protoweaver.api.ProtoWeaver;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.handler.auth.ClientAuthHandler;
import kr.rtuserver.protoweaver.api.protocol.handler.auth.ServerAuthHandler;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.protocol.internal.GlobalPacket;
import kr.rtuserver.protoweaver.api.protocol.serializer.CustomPacketSerializer;
import kr.rtuserver.protoweaver.api.util.ObjectSerializer;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import lombok.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Stores all the registered packets, settings and additional configuration of a {@link ProtoWeaver} protocol.
 */
@EqualsAndHashCode
public class Protocol {

    @EqualsAndHashCode.Exclude
    private final ObjectSerializer serializer = new ObjectSerializer();
    @Getter
    private final MessageDigest packetMD = MessageDigest.getInstance("SHA-1");
    @Getter
    private final String namespace;
    @Getter
    private final String key;
    @Getter
    private CompressionType compression = CompressionType.NONE;
    @Getter
    private int compressionLevel = -37;
    @Getter
    private int maxPacketSize = 16384;
    @Getter
    private int maxConnections = -1;
    @Getter
    private Level loggingLevel = Level.ALL;

    @EqualsAndHashCode.Exclude
    private Constructor<? extends ProtoConnectionHandler> serverConnectionHandler;
    @EqualsAndHashCode.Exclude
    private Object[] serverConnectionHandlerArgs = new Object[0];
    @EqualsAndHashCode.Exclude
    private Constructor<? extends ProtoConnectionHandler> clientConnectionHandler;
    @EqualsAndHashCode.Exclude
    private Object[] clientConnectionHandlerArgs = new Object[0];
    @EqualsAndHashCode.Exclude
    private Constructor<? extends ServerAuthHandler> serverAuthHandler;
    @EqualsAndHashCode.Exclude
    private Object[] serverAuthHandlerArgs = new Object[0];
    @EqualsAndHashCode.Exclude
    private Constructor<? extends ClientAuthHandler> clientAuthHandler;
    @EqualsAndHashCode.Exclude
    private Object[] clientAuthHandlerArgs = new Object[0];

    private Protocol(String namespace, String name) throws NoSuchAlgorithmException {
        this.namespace = namespace;
        this.key = name;
    }

    /**
     * <p>Creates a new protocol builder. A good rule of thumb for naming that ensures maximum compatibility is to use
     * your mod id or project id for the namespace and to give the name something unique.</p>
     * <br>For example: "protoweaver:proto-message"</br>
     *
     * @param namespace Usually should be set to your mod id or project id
     * @param name      The name of your protocol.
     */
    @SneakyThrows
    public static Builder create(@NonNull String namespace, @NonNull String name) {
        return new Builder(new Protocol(namespace, name));
    }

    public String getNamespaceKey() {
        return namespace + ":" + key;
    }

    public boolean isGlobal(Object packet) {
        return packet instanceof GlobalPacket;
    }

    /**
     * Allows you to create modify an existing {@link Protocol}. The {@link Protocol} object returned from
     * {@link Builder#build()} will be the same object as the one that this method was called on (not a copy). In
     * theory this means you can modify a protocol without reloading it, or while its currently active. Here be dragons,
     * so use with caution.
     */
    public Builder modify() {
        return new Builder(this);
    }

    @SneakyThrows
    public ProtoConnectionHandler newConnectionHandler(Side side) {
        return switch (side) {
            case CLIENT -> {
                if (clientConnectionHandler == null)
                    throw new RuntimeException("No client connection handler set for protocol: " + this);
                yield clientConnectionHandler.newInstance(clientConnectionHandlerArgs);
            }
            case SERVER -> {
                if (serverConnectionHandler == null)
                    throw new RuntimeException("No server connection handler set for protocol: " + this);
                yield serverConnectionHandler.newInstance(serverConnectionHandlerArgs);
            }
        };
    }

    @SneakyThrows
    public ServerAuthHandler newServerAuthHandler() {
        if (serverAuthHandler == null) throw new RuntimeException("No server auth handler set for protocol: " + this);
        return serverAuthHandler.newInstance(serverAuthHandlerArgs);
    }

    @SneakyThrows
    public ClientAuthHandler newClientAuthHandler() {
        if (clientAuthHandler == null) throw new RuntimeException("No client auth handler set for protocol: " + this);
        return clientAuthHandler.newInstance(clientAuthHandlerArgs);
    }

    public byte[] serialize(@NonNull Object packet) throws IllegalArgumentException {
        return serializer.serialize(packet);
    }

    public Object deserialize(byte @NonNull [] packet) throws IllegalArgumentException {
        return serializer.deserialize(packet);
    }

    @SneakyThrows
    public byte[] getSHA1() {
        MessageDigest md = (MessageDigest) this.packetMD.clone();
        md.update(toString().getBytes(StandardCharsets.UTF_8));
        md.update(ByteBuffer.allocate(12)
                .putInt(compressionLevel)
                .putInt(compression.ordinal())
                .putInt(maxPacketSize)
                .array());
        return md.digest();
    }

    /**
     * @return The number of connected clients this protocol is currently serving.
     */
    public int getConnections() {
        return ProtoConnection.getConnectionCount(this);
    }

    /**
     * Determine if a side requires auth by checking to see if an auth handler was set for the given side.
     *
     * @param side The {@link Side} to check for an auth handler.
     */
    public boolean requiresAuth(@NonNull Side side) {
        if (side.equals(Side.CLIENT)) return clientAuthHandler != null;
        return serverAuthHandler != null;
    }

    public void logInfo(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.INFO.intValue()) ProtoLogger.info("[" + this + "] " + message);
    }

    public void logWarn(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.WARNING.intValue()) ProtoLogger.warn("[" + this + "] " + message);
    }

    public void logErr(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.SEVERE.intValue()) ProtoLogger.err("[" + this + "] " + message);
    }

    @Override
    public String toString() {
        return namespace + ":" + key;
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
         * Set the packets handler that the server will use to process inbound packets.
         *
         * @param handler The class of the packets handler.
         */
        @SneakyThrows
        public Builder setServerHandler(Class<? extends ProtoConnectionHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.serverConnectionHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.serverConnectionHandlerArgs = args;
            return this;
        }

        /**
         * Set the packets handler that the client will use to process inbound packets.
         *
         * @param handler The class of the packets handler.
         */
        @SneakyThrows
        public Builder setClientHandler(Class<? extends ProtoConnectionHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.clientConnectionHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.clientConnectionHandlerArgs = args;
            return this;
        }

        /**
         * Set the auth handler that the server will use to process inbound client secrets.
         *
         * @param handler The class of the auth handler.
         */
        @SneakyThrows
        public Builder setServerAuthHandler(Class<? extends ServerAuthHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.serverAuthHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.serverAuthHandlerArgs = args;
            return this;
        }

        /**
         * Set the auth handler that the client will use to get the secret that will be sent to the server.
         *
         * @param handler The class of the auth handler.
         */
        @SneakyThrows
        public Builder setClientAuthHandler(Class<? extends ClientAuthHandler> handler, Object... args) {
            if (Modifier.isAbstract(handler.getModifiers()))
                throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            protocol.clientAuthHandler = handler.getDeclaredConstructor(getArgTypes(args));
            protocol.clientAuthHandlerArgs = args;
            return this;
        }

        /**
         * Register a class to the {@link Protocol}. Does nothing if the class has already been registered.
         *
         * @param packet The packets to register.
         */
        public Builder addPacket(@NonNull Class<?> packet) {
            protocol.serializer.register(packet);
            if (!packets.contains(packet)) {
                protocol.packetMD.update(packet.getName().getBytes(StandardCharsets.UTF_8));
                packets.add(packet);
            }
            return this;
        }

        /**
         * Register a class to the {@link Protocol} with a custom serializer. Does nothing if the class has already been registered.
         *
         * @param packet     The packet to register.
         * @param serializer The custom serializer to register.
         */
        public Builder addPacket(@NonNull Class<?> packet, @NonNull Class<? extends ProtoSerializer<?>> serializer) {
            Class<?> type = (serializer == CustomPacketSerializer.class) ? CustomPacket.class : packet;
            protocol.serializer.register(packet, serializer);
            if (!packets.contains(type)) {
                protocol.packetMD.update(type.getName().getBytes(StandardCharsets.UTF_8));
                packets.add(type);
            }
            return this;
        }

        /**
         * Enables compression on the {@link Protocol}. The compression type by defaults is set to {@link CompressionType#NONE}.
         *
         * @param type The type of compression to enable.
         */
        public Builder setCompression(@NonNull CompressionType type) {
            protocol.compression = type;
            return this;
        }

        /**
         * Set the compression level if compression is enabled. Be sure to check the supported level for each type of
         * compression online.
         *
         * @param level The compression level to set.
         */
        public Builder setCompressionLevel(int level) {
            protocol.compressionLevel = level;
            return this;
        }

        /**
         * Set the maximum packets size this {@link Protocol} can handle. The higher the value, the more ram will be
         * allocated when sending and receiving packets. The maximum packets size defaults to 16kb.
         *
         * @param maxPacketSize The maximum size a packets can be in bytes.
         */
        public Builder setMaxPacketSize(int maxPacketSize) {
            protocol.maxPacketSize = maxPacketSize;
            return this;
        }

        /**
         * Set the number of maximum concurrent connections this {@link Protocol} will allow. Any connections over this limit
         * will be disconnected. The maximum connections defaults to -1 and allows any number of connections.
         *
         * @param maxConnections The maximum concurrent connections.
         */
        public Builder setMaxConnections(int maxConnections) {
            protocol.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets the logging level for this {@link Protocol}.
         */
        public Builder setLoggingLevel(Level level) {
            protocol.loggingLevel = level;
            return this;
        }

        /**
         * Build the {@link Protocol}.
         *
         * @return A finished protocol that can be loaded using {@link ProtoWeaver#load(Protocol)}.
         */
        public Protocol build() {
            if (protocol.compression != CompressionType.NONE && protocol.compressionLevel == -37)
                protocol.compressionLevel = protocol.compression.getDefaultLevel();
            return protocol;
        }

        /**
         * Equivalent to calling {@link Builder#build()} and {@link ProtoWeaver#load(Protocol)}.
         *
         * @return The {@link Protocol} that was built and loaded.
         */
        public Protocol load() {
            ProtoWeaver.load(build());
            return protocol;
        }
    }
}