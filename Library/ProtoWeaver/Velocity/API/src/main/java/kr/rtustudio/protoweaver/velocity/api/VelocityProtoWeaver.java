package kr.rtustudio.protoweaver.velocity.api;

import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.protoweaver.api.protocol.Packet;
import kr.rtustudio.protoweaver.api.proxy.ServerSupplier;
import kr.rtustudio.protoweaver.api.util.ProtoLogger;

import java.nio.file.Path;
import java.util.Set;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;

public interface VelocityProtoWeaver extends ProtoLogger.IProtoLogger, ServerSupplier {

    ProxyServer getServer();

    Toml getVelocityConfig();

    Path getDir();

    void registerProtocol(
            String namespace,
            String key,
            Packet packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback);

    void registerProtocol(
            String namespace,
            String key,
            Set<Packet> packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback);
}
