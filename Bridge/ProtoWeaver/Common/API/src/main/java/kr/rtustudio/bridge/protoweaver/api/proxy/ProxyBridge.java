package kr.rtustudio.bridge.protoweaver.api.proxy;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.protoweaver.api.serializer.ProtoSerializer;
import kr.rtustudio.bridge.protoweaver.api.util.ProtoLogger;

import java.util.function.Consumer;

/**
 * Proxy-side bridge for ProtoWeaver platforms (Velocity, BungeeCord).
 *
 * <pre>{@code
 * bridge.register("rsf:shop", p -> {
 *     p.register(BuyPacket.class);
 *     p.register(SellPacket.class, MySellSerializer.class);
 * });
 * bridge.subscribe("rsf:shop", packet -> { ... });
 * bridge.publish("rsf:shop", new BuyPacket(...));
 * }</pre>
 */
public interface ProxyBridge extends ProtoLogger.IProtoLogger, ServerSupplier {

    void register(BridgeChannel channel, Consumer<PacketRegistrar> registrar);

    void subscribe(BridgeChannel channel, Consumer<Object> handler);

    void publish(BridgeChannel channel, Object message);

    void unsubscribe(BridgeChannel channel);

    void shutdown();

    interface PacketRegistrar {

        void register(Class<?> type);

        void register(Class<?> type, Class<? extends ProtoSerializer<?>> serializer);
    }
}
