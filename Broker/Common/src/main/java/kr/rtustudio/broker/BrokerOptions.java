package kr.rtustudio.broker;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.fory.Fory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.logging.LoggerFactory;
import org.xerial.snappy.Snappy;

public final class BrokerOptions {

    static {
        LoggerFactory.disableLogging();
    }

    private final Fory fory;
    private final boolean compress;
    private final boolean tls;

    private BrokerOptions(Builder builder) {
        this.fory =
                Fory.builder()
                        .withJdkClassSerializableCheck(false)
                        .withDeserializeUnknownClass(false)
                        .withLanguage(Language.JAVA)
                        .withCompatibleMode(CompatibleMode.COMPATIBLE)
                        .withAsyncCompilation(true)
                        .withClassLoader(builder.classLoader)
                        .build();
        this.compress = builder.compress;
        this.tls = builder.tls;
    }

    public boolean isCompress() {
        return compress;
    }

    public boolean isTls() {
        return tls;
    }

    public void register(Class<?> type) {
        recursiveRegister(type, new ArrayList<>());
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

    public byte[] encode(String channel, Object value) {
        byte[] serialized;
        synchronized (fory) {
            try {
                serialized = fory.serialize(value);
            } catch (InsecureException e) {
                throw new IllegalArgumentException(
                        "Unregistered type: " + value.getClass().getName(), e);
            }
        }
        byte[] payload = compress ? snappyCompress(serialized) : serialized;
        byte[] channelBytes = channel.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + channelBytes.length + payload.length);
        buf.putInt(channelBytes.length);
        buf.put(channelBytes);
        buf.put(payload);
        return buf.array();
    }

    public String peekChannel(byte[] frame) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(frame);
            int len = buf.getInt();
            if (len < 0 || len > buf.remaining()) return null;
            byte[] channelBytes = new byte[len];
            buf.get(channelBytes);
            return new String(channelBytes, StandardCharsets.UTF_8);
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
        byte[] decompressed = compress ? snappyDecompress(payload) : payload;
        synchronized (fory) {
            try {
                return fory.deserialize(decompressed);
            } catch (InsecureException e) {
                String name = e.getMessage().replace("class ", "").split(" is not registered")[0];
                throw new IllegalArgumentException("Unregistered type: " + name, e);
            }
        }
    }

    private static byte[] snappyCompress(byte[] data) {
        try {
            return Snappy.compress(data);
        } catch (Exception e) {
            throw new RuntimeException("Snappy compress failed", e);
        }
    }

    private static byte[] snappyDecompress(byte[] data) {
        try {
            return Snappy.uncompress(data);
        } catch (Exception e) {
            throw new RuntimeException("Snappy decompress failed", e);
        }
    }

    public static Builder builder(ClassLoader classLoader) {
        return new Builder(classLoader);
    }

    public static BrokerOptions defaults(ClassLoader classLoader) {
        return builder(classLoader).build();
    }

    public static final class Builder {

        private final ClassLoader classLoader;
        private boolean compress = false;
        private boolean tls = true;

        private Builder(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public Builder compress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public Builder tls(boolean tls) {
            this.tls = tls;
            return this;
        }

        public BrokerOptions build() {
            return new BrokerOptions(this);
        }
    }
}
