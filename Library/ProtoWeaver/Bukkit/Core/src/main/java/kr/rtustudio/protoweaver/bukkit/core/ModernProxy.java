package kr.rtustudio.protoweaver.bukkit.core;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import kr.rtustudio.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.protoweaver.core.loader.netty.ProtoDeterminer;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;

import java.nio.charset.StandardCharsets;

import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j(topic = "RSF/ProtoWeaver")
public class ModernProxy implements ChannelInitializeListener {

    public static void initialize() {
        log.info("Detected modern proxy");
        ChannelInitializeListenerHolder.addListener(
                Key.key("rsframework", "protoweaver"), new ModernProxy());
        VelocityAuth.setSecret(
                GlobalConfiguration.get().proxies.velocity.secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ProtoDeterminer.registerToPipeline(channel);
    }
}
