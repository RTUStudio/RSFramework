package kr.rtustudio.bridge;

/**
 * Redis-based bridge interface.
 *
 * <pre>{@code
 * RedisBridge bridge = registry.get(RedisBridge.class);
 * bridge.publish("rsf:shop", new BuyPacket(...));
 * }</pre>
 */
public interface Redis extends Bridge {}
