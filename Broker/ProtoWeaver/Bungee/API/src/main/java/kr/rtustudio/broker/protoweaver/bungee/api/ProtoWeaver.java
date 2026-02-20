package kr.rtustudio.broker.protoweaver.bungee.api;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyBroker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;

import java.nio.file.Path;

public interface ProtoWeaver extends Listener, ProxyBroker {

    ProxyServer getServer();

    Path getDir();
}
