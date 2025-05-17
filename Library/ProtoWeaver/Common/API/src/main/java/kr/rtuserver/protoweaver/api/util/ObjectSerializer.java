package kr.rtuserver.protoweaver.api.util;

import com.google.gson.Gson;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.r.R;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.exception.InsecureException;
import org.apache.fury.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class ObjectSerializer {

    private final static Set<Class<?>> list = new CopyOnWriteArraySet<>();

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private final ThreadSafeFury fury = Fury.builder().withJdkClassSerializableCheck(false).buildThreadSafeFury();
    private final Gson GSON = new Gson();
    private final Map<String, Boolean> packetMap = new HashMap<>();

    private void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null || type == Object.class || registered.contains(type)) return;
        fury.register(type);
        registered.add(type);
        list.add(type);

        List.of(type.getDeclaredFields()).forEach(field -> recursiveRegister(field.getType(), registered));
        List.of(R.of(type).generics()).forEach(t -> recursiveRegister(t, registered));
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
    }

    public void register(Class<?> type, boolean isBothSide) {
        packetMap.put(type.getName(), isBothSide);
        recursiveRegister(isBothSide ? type : CustomPacket.class, new ArrayList<>()); // TODO
    }

    public byte[] serialize(Object object, ProtoConnectionHandler handler) throws IllegalArgumentException {
        try {
            if (!packetMap.getOrDefault(object.getClass().getName(), false)) {
                return fury.serialize(new CustomPacket(object.getClass().getName(), handler.getClass().getName(), GSON.toJson(object)));
            } else return fury.serialize(object);
        } catch (InsecureException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("unregistered object: " + object.getClass().getName());
        }
    }

    public Object deserialize(byte[] bytes) throws IllegalArgumentException {
        try {
            Object result = fury.deserialize(bytes);
            if (result instanceof CustomPacket custom) {
                try {
                    return GSON.fromJson(custom.json(), Class.forName(custom.classType()));
                } catch (ClassNotFoundException e) {
                    return result;
                }
            } else return result;
        } catch (InsecureException e) {
            String packet = e.getMessage().split(" is not registered")[0].replace("class ", "");
            throw new IllegalArgumentException("unregistered object: " + packet);
        }
    }
}