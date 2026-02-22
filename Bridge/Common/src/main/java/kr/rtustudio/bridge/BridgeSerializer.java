package kr.rtustudio.bridge;

/**
 * Custom byte-level serializer for bridge packet types. Used with {@link
 * BridgeCodec#register(String, Class, BridgeSerializer)} when default Jackson/MsgPack serialization
 * is insufficient (e.g. complex Bukkit types).
 *
 * <p>The serialized {@code byte[]} is embedded in the MsgPack frame under the {@code __data} field
 * and extracted verbatim on the receiving side.
 *
 * @param <T> the type to serialize/deserialize
 */
public interface BridgeSerializer<T> {

    /**
     * Serialize the given value to a byte array.
     *
     * @param value the object to serialize
     * @return serialized bytes
     */
    byte[] serialize(T value);

    /**
     * Deserialize the given byte array to an object.
     *
     * @param bytes the serialized bytes
     * @return deserialized object
     */
    T deserialize(byte[] bytes);
}
