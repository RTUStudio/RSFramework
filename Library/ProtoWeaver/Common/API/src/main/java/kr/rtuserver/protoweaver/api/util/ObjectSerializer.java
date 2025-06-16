package kr.rtuserver.protoweaver.api.util;

import com.google.gson.Gson;
import kr.rtuserver.protoweaver.api.ProtoSerializerAdapter;
import kr.rtuserver.protoweaver.api.ProtoSerializer;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.protocol.serializer.CustomPacketSerializer;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.exception.InsecureException;
import org.apache.fury.logging.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectSerializer {

    private static final Gson GSON = new Gson();

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private final Fury fury = Fury.builder()
            .withJdkClassSerializableCheck(false)
            .withDeserializeNonexistentClass(false)
            .withLanguage(Language.JAVA)
            .withCompatibleMode(CompatibleMode.COMPATIBLE)
            .withAsyncCompilation(true)
            .withClassLoader(ProtoSerializer.class.getClassLoader())
            .build();

    private final Set<Class<?>> customPackets = new HashSet<>();

    private void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null || type == Object.class || registered.contains(type) || Modifier.isAbstract(type.getModifiers()))
            return;
        synchronized (fury) {
            fury.register(type);
        }
        registered.add(type);

        List.of(type.getDeclaredFields()).forEach(field -> recursiveRegister(field.getType(), registered));
        List.of(ReflectionUtil.of(type).generics()).forEach(t -> recursiveRegister(t, registered));
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
    }

    public void register(Class<?> type) {
        recursiveRegister(type, new ArrayList<>());
    }

    @SneakyThrows
    public void register(Class<?> type, Class<? extends ProtoSerializer<?>> serializer) {
        synchronized (fury) {
            if (type != CustomPacket.class && serializer == CustomPacketSerializer.class) customPackets.add(type);
            fury.registerSerializer(CustomPacket.class, new ProtoSerializerAdapter<>(fury, CustomPacket.class, serializer));
        }
    }

    public byte[] serialize(Object object) throws IllegalArgumentException {
        synchronized (fury) {
            try {
                if (customPackets.contains(object.getClass())) {
                    return fury.serialize(new CustomPacket(object));
                } else return fury.serialize(object);
            } catch (InsecureException e) {
                throw new IllegalArgumentException("unregistered object: " + object.getClass().getName());
            }
        }
    }

    public Object deserialize(byte[] bytes) throws IllegalArgumentException {
        synchronized (fury) {
            try {
                Object result = fury.deserialize(bytes);
                if (result instanceof CustomPacket(String classType, String json)) {
                    try {
                        Class<?> type = Class.forName(classType);
                        if (customPackets.contains(type)) return GSON.fromJson(json, type);
                    } catch (ClassNotFoundException ignore) {
                    }
                }
                return result;
            } catch (InsecureException e) {
                String packetName = e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("unregistered object: " + packetName, e);
            }
        }
    }
}