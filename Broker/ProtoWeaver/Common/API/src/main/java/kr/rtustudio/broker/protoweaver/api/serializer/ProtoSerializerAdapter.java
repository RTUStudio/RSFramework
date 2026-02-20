package kr.rtustudio.broker.protoweaver.api.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

public class ProtoSerializerAdapter<T> extends Serializer<T> {

    private final ProtoSerializer<T> streamSerializer;

    public ProtoSerializerAdapter(
            Fory fory, Class<T> type, Class<? extends ProtoSerializer<?>> streamSerializer) {
        super(fory, type);
        try {
            this.streamSerializer =
                    (ProtoSerializer<T>) streamSerializer.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T read(MemoryBuffer buffer) {
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.getRemainingBytes());
        return streamSerializer.read(in);
    }

    @Override
    public void write(MemoryBuffer buffer, T value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamSerializer.write(out, value);
        buffer.writeBytes(out.toByteArray());
    }
}
