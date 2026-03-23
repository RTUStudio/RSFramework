package kr.rtustudio.bridge;

/**
 * Generic serializer/deserializer interface for bridge communication.
 *
 * <p>브릿지 통신용 범용 직렬화/역직렬화 인터페이스.
 *
 * @param <T> serialization target type
 */
public interface BridgeSerializer<T> {

    byte[] serialize(T value);

    T deserialize(byte[] bytes);
}
