package kr.rtustudio.bridge;

public interface BridgeSerializer<T> {

    byte[] serialize(T value);

    T deserialize(byte[] bytes);
}
