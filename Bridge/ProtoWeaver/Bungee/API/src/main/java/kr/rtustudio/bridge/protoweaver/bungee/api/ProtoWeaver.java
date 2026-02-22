package kr.rtustudio.bridge.protoweaver.bungee.api;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyBridge;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;

import java.nio.file.Path;

public interface ProtoWeaver extends Listener, ProxyBridge {

    ProxyServer getServer();

    Path getDir();
}
