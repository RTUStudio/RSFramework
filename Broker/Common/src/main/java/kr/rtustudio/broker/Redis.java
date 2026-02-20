package kr.rtustudio.broker;

/**
 * Redis-based broker interface.
 *
 * <pre>{@code
 * RedisBroker broker = registry.get(RedisBroker.class);
 * broker.publish("rsf:shop", new BuyPacket(...));
 * }</pre>
 */
public interface Redis extends Broker {}
