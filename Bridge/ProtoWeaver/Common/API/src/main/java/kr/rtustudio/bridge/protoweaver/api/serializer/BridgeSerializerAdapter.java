package kr.rtustudio.bridge.protoweaver.api.serializer;

import kr.rtustudio.bridge.BridgeSerializer;

import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Adapts a {@link BridgeSerializer} to Fory's {@link Serializer}. Used internally to bridge unified
 * bridge serializers into ProtoWeaver's binary protocol.
 *
 * @param <T> the type to serialize/deserialize
 */
public class BridgeSerializerAdapter<T> extends Serializer<T> {

    private final BridgeSerializer<T> bridgeSerializer;

    public BridgeSerializerAdapter(Fory fory, Class<T> type, BridgeSerializer<T> bridgeSerializer) {
        super(fory, type);
        this.bridgeSerializer = bridgeSerializer;
    }

    @Override
    public T read(MemoryBuffer buffer) {
        byte[] bytes = buffer.getRemainingBytes();
        return bridgeSerializer.deserialize(bytes);
    }

    @Override
    public void write(MemoryBuffer buffer, T value) {
        byte[] bytes = bridgeSerializer.serialize(value);
        buffer.writeBytes(bytes);
    }
}
