package kr.rtustudio.bridge.protoweaver.bungee.api;

import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyBridge;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.nio.file.Path;

public interface ProtoWeaver extends Listener, ProxyBridge {

    static void sendMessage(ProxiedPlayer player, String minimessage) {
        player.sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(minimessage));
    }

    ProxyServer getServer();

    Path getDir();

    void ready(ProtoConnection connection);
}
