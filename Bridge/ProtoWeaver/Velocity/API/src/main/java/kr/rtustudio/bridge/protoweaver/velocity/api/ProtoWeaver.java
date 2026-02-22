package kr.rtustudio.bridge.protoweaver.velocity.api;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyBridge;

import java.nio.file.Path;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;

public interface ProtoWeaver extends ProxyBridge {

    ProxyServer getServer();

    Toml getVelocityConfig();

    Path getDir();
}
