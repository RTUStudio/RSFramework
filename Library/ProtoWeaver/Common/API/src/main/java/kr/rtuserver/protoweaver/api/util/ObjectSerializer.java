package kr.rtuserver.protoweaver.api.util;

import com.google.gson.Gson;
import kr.rtuserver.protoweaver.api.ProtoSerializer;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.protocol.serializer.CustomPacketSerializer;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.exception.InsecureException;
import org.apache.fury.logging.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class ObjectSerializer {

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private static final Gson GSON = new Gson();
    private final Fury fury = Fury.builder()
            .withJdkClassSerializableCheck(false)
            .withDeserializeNonexistentClass(false)
            .withLanguage(Language.JAVA)
            .withCompatibleMode(CompatibleMode.COMPATIBLE)
            .withAsyncCompilation(true)
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
        try {
            serializer.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException ignored) {
            serializer.getDeclaredConstructor(Class.class).newInstance(type);
        }
        synchronized (fury) {
            if (serializer == CustomPacketSerializer.class) customPackets.add(type);
            fury.registerSerializer(type, ProtoSerializer.SerializerWrapper.class);
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
                    return GSON.fromJson(json, Class.forName(classType));
                } else return result;
            } catch (InsecureException | ClassNotFoundException e) {
                String packet = e.getMessage().split(" is not registered")[0].replace("class ", "");
                throw new IllegalArgumentException("unregistered object: " + packet);
            }
        }
    }
}