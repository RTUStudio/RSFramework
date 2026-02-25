package kr.rtustudio.bridge.proxium.core;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

@Slf4j(topic = "Proxium")
@Getter
public abstract class AbstractProxium implements Bridge {

    protected final BridgeOptions options;
    protected final Set<BridgeChannel> registeredChannels = ConcurrentHashMap.newKeySet();
    protected final Map<BridgeChannel, Consumer<Object>> channelHandlers =
            new ConcurrentHashMap<>();

    protected AbstractProxium(BridgeOptions options) {
        this.options = options;
    }

    @Override
    public void register(BridgeChannel channel, Class<?>... types) {
        options.register(channel, types);
        registeredChannels.add(channel);
        onChannelRegistered(channel);
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        channelHandlers.merge(channel, handler, Consumer::andThen);
    }

    @Override
    public void unsubscribe(BridgeChannel channel) {
        channelHandlers.remove(channel);
        registeredChannels.remove(channel);
    }

    /**
     * 바이트 프레임을 디코딩하고, 해당 채널의 핸들러에 디스패치한다.
     *
     * @return 디코딩된 객체 (핸들러가 없거나 채널이 null이면 null)
     */
    @Nullable
    public Object dispatchPacket(byte[] frame) {
        BridgeChannel channel = options.peekChannel(frame);
        if (channel == null) {
            log.warn("Received unroutable Proxium frame");
            return null;
        }
        Object decoded;
        try {
            decoded = options.decode(frame);
        } catch (Exception e) {
            log.error("Failed to decode frame on channel: {}", channel, e);
            return null;
        }
        Consumer<Object> handler = channelHandlers.get(channel);
        if (handler != null) {
            try {
                handler.accept(decoded);
            } catch (Exception e) {
                log.error("Error in handler for channel: {}", channel, e);
            }
        }
        return decoded;
    }

    /** 바이트 프레임의 채널만 조회한다. */
    @Nullable
    public BridgeChannel peekChannel(byte[] frame) {
        return options.peekChannel(frame);
    }

    protected void onChannelRegistered(BridgeChannel channel) {}
}
