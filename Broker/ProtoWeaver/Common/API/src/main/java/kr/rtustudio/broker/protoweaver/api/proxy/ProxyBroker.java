package kr.rtustudio.broker.protoweaver.api.proxy;

import kr.rtustudio.broker.protoweaver.api.serializer.ProtoSerializer;
import kr.rtustudio.broker.protoweaver.api.util.ProtoLogger;

import java.util.function.Consumer;

/**
 * Proxy-side broker for ProtoWeaver platforms (Velocity, BungeeCord).
 *
 * <pre>{@code
 * broker.register("rsf:shop", p -> {
 *     p.register(BuyPacket.class);
 *     p.register(SellPacket.class, MySellSerializer.class);
 * });
 * broker.subscribe("rsf:shop", packet -> { ... });
 * broker.publish("rsf:shop", new BuyPacket(...));
 * }</pre>
 */
public interface ProxyBroker extends ProtoLogger.IProtoLogger, ServerSupplier {

    void register(String channel, Consumer<PacketRegistrar> registrar);

    void subscribe(String channel, Consumer<Object> handler);

    void publish(String channel, Object message);

    void unsubscribe(String channel);

    void shutdown();

    interface PacketRegistrar {

        void register(Class<?> type);

        void register(Class<?> type, Class<? extends ProtoSerializer<?>> serializer);
    }
}
