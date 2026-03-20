package kr.rtustudio.bridge;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.fory.Fory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.logging.LoggerFactory;

public final class BridgeOptions {

    static {
        LoggerFactory.disableLogging();
    }

    private final Fory fory;
    private final Map<BridgeChannel, ConcurrentSkipListSet<String>> channelTypes =
            new ConcurrentHashMap<>();
    @Getter private final boolean tls;

    private BridgeOptions(Builder builder) {
        this.fory =
                Fory.builder()
                        .withJdkClassSerializableCheck(false)
                        .withDeserializeUnknownClass(false)
                        .withLanguage(Language.JAVA)
                        .withCompatibleMode(CompatibleMode.COMPATIBLE)
                        .withAsyncCompilation(true)
                        .withClassLoader(builder.classLoader)
                        .build();
        this.tls = builder.tls;
    }

    public static Builder builder(ClassLoader classLoader) {
        return new Builder(classLoader);
    }

    public static BridgeOptions defaults(ClassLoader classLoader) {
        return builder(classLoader).build();
    }

    public void register(BridgeChannel channel, Class<?>... types) {
        ConcurrentSkipListSet<String> set =
                channelTypes.computeIfAbsent(channel, k -> new ConcurrentSkipListSet<>());
        for (Class<?> type : types) {
            set.add(type.getName());
            recursiveRegister(type, new ArrayList<>());
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

    private void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null
                || type == Object.class
                || registered.contains(type)
                || Modifier.isAbstract(type.getModifiers())) return;
        synchronized (fory) {
            fory.register(type);
        }
        registered.add(type);
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            recursiveRegister(field.getType(), registered);
        }
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
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
        byte[] payload = serialized;
        byte[] channelBytes = channel.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + channelBytes.length + payload.length);
        buf.putInt(channelBytes.length);
        buf.put(channelBytes);
        buf.put(payload);
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
        byte[] decompressed = payload;
        synchronized (fory) {
            try {
                return fory.deserialize(decompressed);
            } catch (InsecureException e) {
                String name = e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("Unregistered type: " + name, e);
            }
        }
    }

    public static final class Builder {

        private final ClassLoader classLoader;
        private boolean tls = true;

        private Builder(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public Builder tls(boolean tls) {
            this.tls = tls;
            return this;
        }

        public BridgeOptions build() {
            return new BridgeOptions(this);
        }
    }
}
