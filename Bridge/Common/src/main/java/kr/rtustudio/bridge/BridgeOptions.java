package kr.rtustudio.bridge;

import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.fory.Fory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.logging.LoggerFactory;

/**
 * Manages Fory-based serialization for bridge packet encoding/decoding. Handles channel-scoped type
 * registration and SHA-1 fingerprinting.
 *
 * <p>브릿지 패킷 인코딩/디코딩을 위한 Fory 기반 직렬화 관리 클래스. 채널 단위 타입 등록 및 SHA-1 핑거프린팅을 처리한다.
 */
public final class BridgeOptions {

    static {
        LoggerFactory.disableLogging();
    }

    private final Fory fory;
    private final Map<BridgeChannel, ConcurrentSkipListSet<String>> channelTypes =
            new ConcurrentHashMap<>();

    public BridgeOptions(ClassLoader classLoader) {
        this.fory =
                Fory.builder()
                        .withJdkClassSerializableCheck(false)
                        .withDeserializeUnknownClass(false)
                        .withLanguage(Language.JAVA)
                        .withCompatibleMode(CompatibleMode.COMPATIBLE)
                        .withAsyncCompilation(false)
                        .withClassLoader(classLoader)
                        .build();
    }

    public void register(BridgeChannel channel, Class<?>... types) {
        ConcurrentSkipListSet<String> set =
                channelTypes.computeIfAbsent(channel, k -> new ConcurrentSkipListSet<>());
        for (Class<?> type : types) {
            set.add(type.getName());
            recursiveRegister(type, new HashSet<>());
        }
    }

    @SneakyThrows
    public byte[] getChannelSHA1(BridgeChannel channel) {
        ConcurrentSkipListSet<String> set = channelTypes.get(channel);
        if (set == null || set.isEmpty()) return new byte[0];
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        for (String name : set) {
            md.update(name.getBytes(StandardCharsets.UTF_8));
        }
        return md.digest();
    }

    private void recursiveRegister(Class<?> type, Set<Class<?>> registered) {
        if (type == null
                || type == Object.class
                || !registered.add(type)
                || Modifier.isAbstract(type.getModifiers())) return;
        synchronized (fory) {
            fory.register(type);
        }
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            recursiveRegister(field.getType(), registered);
        }
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
    }

    /**
     * Serializes an object to a raw byte array without channel header. Used for protocol
     * handshakes.
     *
     * <p>객체를 채널 헤더 없이 순수 바이트 배열로 직렬화한다. Protocol 내부 핸드셰이크에서 사용.
     */
    public byte[] serializeRaw(Object value) {
        synchronized (fory) {
            try {
                return fory.serialize(value);
            } catch (InsecureException e) {
                throw new IllegalArgumentException(
                        "Unregistered type: " + value.getClass().getName(), e);
            }
        }
    }

    /**
     * Deserializes a raw byte array back to an object. Used for protocol handshakes.
     *
     * <p>순수 바이트 배열을 객체로 역직렬화한다. Protocol 내부 핸드셰이크에서 사용.
     */
    public Object deserializeRaw(byte[] data) {
        synchronized (fory) {
            try {
                return fory.deserialize(data);
            } catch (InsecureException e) {
                String name = e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("Unregistered type: " + name, e);
            }
        }
    }

    public byte[] encode(BridgeChannel channel, Object value) {
        byte[] serialized;
        synchronized (fory) {
            try {
                serialized = fory.serialize(value);
            } catch (InsecureException e) {
                throw new IllegalArgumentException(
                        "Unregistered type: " + value.getClass().getName(), e);
            }
        }
        byte[] channelBytes = channel.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + channelBytes.length + serialized.length);
        buf.putInt(channelBytes.length);
        buf.put(channelBytes);
        buf.put(serialized);
        return buf.array();
    }

    public BridgeChannel peekChannel(byte[] frame) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(frame);
            int len = buf.getInt();
            if (len < 0 || len > buf.remaining()) return null;
            byte[] channelBytes = new byte[len];
            buf.get(channelBytes);
            return BridgeChannel.of(new String(channelBytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    public Object decode(byte[] frame) {
        ByteBuffer buf = ByteBuffer.wrap(frame);
        int len = buf.getInt();
        buf.position(4 + len);
        byte[] payload = new byte[buf.remaining()];
        buf.get(payload);
        synchronized (fory) {
            try {
                return fory.deserialize(payload);
            } catch (InsecureException e) {
                String name = e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("Unregistered type: " + name, e);
            }
        }
    }
}
