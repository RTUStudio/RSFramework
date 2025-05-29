package kr.rtuserver.protoweaver.api.util;

import com.google.gson.Gson;
import kr.rtuserver.protoweaver.api.ProtoSerializer;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.protocol.serializer.CustomPacketSerializer;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.exception.InsecureException;
import org.apache.fury.logging.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectSerializer {

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private final Gson GSON = new Gson();
    private final Fury fury = Fury.builder()
            .withJdkClassSerializableCheck(false)
            .withDeserializeNonexistentClass(false)
            .withLanguage(Language.JAVA)
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
        System.out.println("[r] " + type.getName());
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
            System.out.println("[R] " + type.getName() + ": " + serializer.getName());
            if (serializer == CustomPacketSerializer.class) customPackets.add(type);
            fury.registerSerializer(type, ProtoSerializer.SerializerWrapper.class);
            System.out.println("[L] " + String.join(", ", customPackets.stream().map(Class::getName).toList()));
        }
    }

    public byte[] serialize(Object object) throws IllegalArgumentException {
        synchronized (fury) {
            try {
                System.out.println("[S] " + object.getClass().getName() + ": " + object);
                System.out.println("[L] " + String.join(", ", customPackets.stream().map(Class::getName).toList()));
                if (customPackets.contains(object.getClass())) {
                    System.out.println("[C] " + new CustomPacket(object));
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
                System.out.println("[D] " + result.getClass().getName() + ": " + result);
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