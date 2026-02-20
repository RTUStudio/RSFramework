package kr.rtustudio.broker.protoweaver.api.serializer;

import kr.rtustudio.broker.BrokerSerializer;

import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Adapts a {@link BrokerSerializer} to Fory's {@link Serializer}. Used internally to bridge unified
 * broker serializers into ProtoWeaver's binary protocol.
 *
 * @param <T> the type to serialize/deserialize
 */
public class BrokerSerializerAdapter<T> extends Serializer<T> {

    private final BrokerSerializer<T> brokerSerializer;

    public BrokerSerializerAdapter(Fory fory, Class<T> type, BrokerSerializer<T> brokerSerializer) {
        super(fory, type);
        this.brokerSerializer = brokerSerializer;
    }

    @Override
    public T read(MemoryBuffer buffer) {
        byte[] bytes = buffer.getRemainingBytes();
        return brokerSerializer.deserialize(bytes);
    }

    @Override
    public void write(MemoryBuffer buffer, T value) {
        byte[] bytes = brokerSerializer.serialize(value);
        buffer.writeBytes(bytes);
    }
}
