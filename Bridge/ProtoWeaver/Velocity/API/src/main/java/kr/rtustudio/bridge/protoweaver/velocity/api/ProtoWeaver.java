package kr.rtustudio.bridge.protoweaver.velocity.api;

import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyBridge;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

public interface ProtoWeaver extends ProxyBridge {

    static void sendMessage(Player player, String minimessage) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(minimessage));
    }

    ProxyServer getServer();

    Toml getVelocityConfig();

    Path getDir();

    void ready(ProtoConnection connection);
}
