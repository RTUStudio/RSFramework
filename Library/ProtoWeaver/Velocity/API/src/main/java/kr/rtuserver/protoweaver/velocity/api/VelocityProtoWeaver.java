package kr.rtuserver.protoweaver.velocity.api;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.proxy.ServerSupplier;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;

import java.nio.file.Path;
import java.util.Set;

public interface VelocityProtoWeaver extends ProtoLogger.IProtoLogger, ServerSupplier {

    ProxyServer getServer();

    Toml getVelocityConfig();

    Path getDir();

    void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

    void registerProtocol(String namespace, String key, Set<Packet> packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);
}
