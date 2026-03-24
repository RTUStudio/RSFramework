package kr.rtustudio.bridge.redis;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kr.rtustudio.bridge.redis.config.RedisConfig;
import kr.rtustudio.bridge.redis.messaging.MessageHandler;
import kr.rtustudio.bridge.redis.messaging.MessageListener;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

@Slf4j
public class Redisson {

    private final RedissonClient client;
    private final Object2ObjectOpenHashMap<String, RTopic> topics =
            new Object2ObjectOpenHashMap<>();
    private final Object2IntOpenHashMap<String> listenerIds = new Object2IntOpenHashMap<>();

    public Redisson(RedisConfig config) {
        Config redissonConfig = new Config();

        if (config.isSentinel()) {
            redissonConfig
                    .useSentinelServers()
                    .setMasterName(config.getSentinelMasterName())
                    .addSentinelAddress(tlsAddresses(config.getSentinelAddresses(), config.isTls()))
                    .setPassword(config.getPassword())
                    .setDatabase(config.getDatabase());
        } else if (config.isCluster()) {
            redissonConfig
                    .useClusterServers()
                    .addNodeAddress(tlsAddresses(config.getNodeAddresses(), config.isTls()))
                    .setPassword(config.getPassword());
        } else {
            SingleServerConfig single =
                    redissonConfig
                            .useSingleServer()
                            .setAddress(tlsAddress(config.getAddress(), config.isTls()))
                            .setDatabase(config.getDatabase());

            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                single.setPassword(config.getPassword());
            }
        }

        this.client = org.redisson.Redisson.create(redissonConfig);
        log.info("Redisson connected to Redis (tls={})", config.isTls());
    }

    private static String tlsAddress(String address, boolean tls) {
        if (!tls || address == null) return address;
        if (address.startsWith("redis://")) return "rediss://" + address.substring(8);
        return address;
    }

    private static String[] tlsAddresses(String[] addresses, boolean tls) {
        if (!tls || addresses == null) return addresses;
        String[] result = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            result[i] = tlsAddress(addresses[i], true);
        }
        return result;
    }

    public <T> void subscribe(String channel, Class<T> type, MessageHandler<T> handler) {
        RTopic topic = client.getTopic(channel, ByteArrayCodec.INSTANCE);
        int listenerId = topic.addListener(type, new MessageListener<>(handler));
        topics.put(channel, topic);
        listenerIds.put(channel, listenerId);
        log.debug("Subscribed to channel: {}", channel);
    }

    public void publish(String channel, Object message) {
        client.getTopic(channel, ByteArrayCodec.INSTANCE).publish(message);
    }

    public org.redisson.api.RLock getLock(String key) {
        return client.getLock(key);
    }

    public void unsubscribe(String channel) {
        RTopic topic = topics.remove(channel);
        int listenerId = listenerIds.removeInt(channel);
        if (topic != null) {
            topic.removeListener(listenerId);
            log.debug("Unsubscribed from channel: {}", channel);
        }
    }

    public void shutdown() {
        topics.forEach(
                (channel, topic) -> {
                    int id = listenerIds.getInt(channel);
                    if (id != 0) topic.removeListener(id);
                });
        topics.clear();
        listenerIds.clear();

        if (client != null && !client.isShutdown()) {
            client.shutdown();
            log.info("Redisson disconnected from Redis");
        }
    }
}
