package kr.rtustudio.broker.protoweaver.api.util;

import kr.rtustudio.broker.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtustudio.broker.protoweaver.api.serializer.CustomPacketSerializer;
import kr.rtustudio.broker.protoweaver.api.serializer.ProtoSerializer;
import kr.rtustudio.broker.protoweaver.api.serializer.ProtoSerializerAdapter;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.fory.Fory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.logging.LoggerFactory;

public class ObjectSerializer {

    static {
        // Make fory be quiet
        LoggerFactory.disableLogging();
    }

    private final Fory fory =
            Fory.builder()
                    .withJdkClassSerializableCheck(false)
                    .withDeserializeUnknownClass(false)
                    .withLanguage(Language.JAVA)
                    .withCompatibleMode(CompatibleMode.COMPATIBLE)
                    .withAsyncCompilation(true)
                    .withClassLoader(ProtoSerializer.class.getClassLoader())
                    .build();

    private final Set<Class<?>> customPackets = new HashSet<>();

    private void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null
                || type == Object.class
                || registered.contains(type)
                || Modifier.isAbstract(type.getModifiers())) return;
        synchronized (fory) {
            fory.register(type);
        }
        registered.add(type);

        List.of(type.getDeclaredFields())
                .forEach(field -> recursiveRegister(field.getType(), registered));
        List.of(ReflectionUtil.of(type).generics()).forEach(t -> recursiveRegister(t, registered));
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
    }

    public void register(Class<?> type) {
        recursiveRegister(type, new ArrayList<>());
    }

    @SneakyThrows
    public void register(Class<?> type, Class<? extends ProtoSerializer<?>> serializer) {
        synchronized (fory) {
            if (type != CustomPacket.class && serializer == CustomPacketSerializer.class)
                customPackets.add(type);
            fory.registerSerializer(
                    CustomPacket.class,
                    new ProtoSerializerAdapter<>(fory, CustomPacket.class, serializer));
        }
    }

    public byte[] serialize(Object object) throws IllegalArgumentException {
        synchronized (fory) {
            try {
                if (customPackets.contains(object.getClass())) {
                    return fory.serialize(new CustomPacket(object));
                } else return fory.serialize(object);
            } catch (InsecureException e) {
                throw new IllegalArgumentException(
                        "unregistered object: " + object.getClass().getName());
            }
        }
    }

    public Object deserialize(byte[] bytes) throws IllegalArgumentException {
        synchronized (fory) {
            try {
                return fory.deserialize(bytes);
            } catch (InsecureException e) {
                String packetName =
                        e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("unregistered object: " + packetName, e);
            }
        }
    }
}
