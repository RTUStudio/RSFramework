package kr.rtustudio.bridge.proxium.bukkit;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.bridge.proxium.core.netty.ConnectionDeterminer;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j(topic = "Proxium")
public class ModernProxy implements ChannelInitializeListener {

    public static void initialize() {
        log.info("Detected modern proxy");
        ChannelInitializeListenerHolder.addListener(
                BridgeChannel.PROXIUM.toKey(), new ModernProxy());
        VelocityAuth.setSecret(
                GlobalConfiguration.get().proxies.velocity.secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ConnectionDeterminer.registerToPipeline(channel);
    }
}
