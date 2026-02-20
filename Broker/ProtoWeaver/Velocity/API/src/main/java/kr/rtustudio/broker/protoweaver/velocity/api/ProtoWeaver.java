package kr.rtustudio.broker.protoweaver.velocity.api;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyBroker;

import java.nio.file.Path;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;

public interface ProtoWeaver extends ProxyBroker {

    ProxyServer getServer();

    Toml getVelocityConfig();

    Path getDir();
}
