package kr.rtuserver.protoweaver.bukkit.core.nms.v1_17_r1;

import com.destroystokyo.paper.PaperConfig;
import io.netty.channel.Channel;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import kr.rtuserver.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtuserver.protoweaver.core.loader.netty.ProtoDeterminer;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j(topic = "RSF/ProtoWeaver")
public class ModernProxy implements ChannelInitializeListener {

    public static void initialize() {
        log.info("Detected modern proxy");
        ChannelInitializeListenerHolder.addListener(Key.key("rsframework", "protoweaver"), new ModernProxy());
        VelocityAuth.setSecret(PaperConfig.velocitySecretKey);
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ProtoDeterminer.registerToPipeline(channel);
    }

}
