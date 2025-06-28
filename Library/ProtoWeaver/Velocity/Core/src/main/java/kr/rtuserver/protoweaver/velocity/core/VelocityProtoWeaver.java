package kr.rtuserver.protoweaver.velocity.core;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.CompressionType;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import kr.rtuserver.protoweaver.api.protocol.internal.*;
import kr.rtuserver.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtuserver.protoweaver.api.proxy.ProtoServer;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.Request;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtuserver.protoweaver.api.serializer.CustomPacketSerializer;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import kr.rtuserver.protoweaver.core.protocol.protoweaver.ServerPacketHandler;
import kr.rtuserver.protoweaver.core.proxy.ProtoProxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class VelocityProtoWeaver implements kr.rtuserver.protoweaver.velocity.api.VelocityProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;

    private final Toml velocityConfig;
    private final Path dir;

    private final ProtoProxy protoProxy;
    private final ConcurrentHashMap<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();
    private final HandlerCallback callable = new HandlerCallback(this::onReady, this::onPacket);

    public VelocityProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);
        velocityConfig = new Toml().read(new File(dir.toFile(), "velocity.toml"));
        protocol = Protocol.create("rsframework", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(Packet.class);

        protocol.addPacket(StorageSync.class);
        protocol.addPacket(BroadcastChat.class);

        protocol.addPacket(ServerName.class);
        protocol.addPacket(ProxyPlayer.class);
        protocol.addPacket(PlayerList.class);
        protocol.addPacket(TeleportRequest.class);
        if (isModernProxy()) {
            info("Detected modern proxy");
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setClientHandler(VelocityProtoHandler.class, callable).load();
    }

    public void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        registerProtocol(namespace, key, Set.of(packet), protocolHandler, callback);
    }

    public void registerProtocol(String namespace, String key, Set<Packet> packets, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        for (Packet packet : packets) {
            if (packet.getSerializer() != null) {
                protocol.addPacket(packet.getPacket(), packet.getSerializer());
            } else protocol.addPacket(packet.getPacket());
        }
        protocol.addPacket(CustomPacket.class, CustomPacketSerializer.class);
        if (isModernProxy()) {
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        if (callback == null) protocol.setClientHandler(protocolHandler).load();
        else protocol.setClientHandler(protocolHandler, callback).load();
    }

    private boolean isModernProxy() {
        String mode = velocityConfig.getString("player-info-forwarding-mode", "");
        if (!List.of("modern", "bungeeguard").contains(mode.toLowerCase())) return false;
        String secretPath = velocityConfig.getString("forwarding-secret-file", "");
        if (secretPath.isEmpty()) return false;
        File file = new File(dir.toFile(), secretPath);
        if (!file.exists() || !file.isFile()) return false;
        try {
            String key = String.join("", Files.readAllLines(file.toPath()));
            return !key.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    @Subscribe
    public void onRegister(ServerRegisteredEvent event) {
        ServerInfo server = event.registeredServer().getServerInfo();
        protoProxy.register(new ProtoServer(server.getName(), server.getAddress()));
    }

    @Subscribe
    public void onUnregister(ServerUnregisteredEvent event) {
        ServerInfo server = event.unregisteredServer().getServerInfo();
        protoProxy.unregister(new ProtoServer(server.getName(), server.getAddress()));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        VelocityProtoHandler.getServers().forEach(server -> server.send(new ServerName("Standalone Server")));
        protoProxy.shutdown();
    }

    @Subscribe
    private void onJoin(ServerPostConnectEvent e) {
        updatePlayerList();
        Player player = e.getPlayer();
        Optional<ServerConnection> scOptional = player.getCurrentServer();
        if (scOptional.isEmpty()) return;
        ServerConnection serverConnection = scOptional.get();
        ServerInfo info = serverConnection.getServerInfo();
        ProtoConnection connection = VelocityProtoHandler.getServer(info.getAddress());
        if (connection == null) return;
        TeleportRequest tpr = teleportRequests.get(player.getUniqueId());
        if (tpr != null) {
            connection.send(tpr);
            teleportRequests.remove(player.getUniqueId());
        }
    }

    @Subscribe
    private void onKick(KickedFromServerEvent e) {
        updatePlayerList();
    }

    @Subscribe
    private void onQuit(DisconnectEvent e) {
        updatePlayerList();
    }

    private void updatePlayerList() {
        Map<UUID, ProxyPlayer> players = new HashMap<>();
        for (Player player : server.getAllPlayers()) {
            Optional<ServerConnection> connection = player.getCurrentServer();
            if (connection.isEmpty()) continue;
            ServerInfo info = connection.get().getServerInfo();
            UUID uuid = player.getUniqueId();
            ProxyPlayer pp = new ProxyPlayer(uuid, info.getName(), player.getUsername());
            players.put(uuid, pp);
        }
        VelocityProtoHandler.getServers().forEach(server -> server.send(new PlayerList(players)));
    }

    @Override
    public List<ProtoServer> getServers() {
        return server.getAllServers().stream().map(server -> new ProtoServer(server.getServerInfo().getName(), server.getServerInfo().getAddress())).toList();
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void err(String message) {
        log.error(message);
    }

    private void onReady(HandlerCallback.Ready data) {
        ProtoConnection connection = data.protoConnection();
        if (!connection.isOpen()) return;
        for (RegisteredServer server : this.server.getAllServers()) {
            ServerInfo info = server.getServerInfo();
            if (connection.getRemoteAddress().equals(info.getAddress())) {
                connection.send(new ServerName(info.getName()));
            }
        }
    }

    private void onPacket(HandlerCallback.Packet data) {
        Object packet = data.packet();
        if (packet instanceof ProtocolRegister(String namespace, String key, Set<Packet> packets)) {
            registerProtocol(namespace, key, packets, ServerPacketHandler.class, null);
        } else if (packet instanceof TeleportRequest request) {
            Optional<RegisteredServer> optionalServer = this.server.getServer(request.server());
            if (optionalServer.isEmpty()) return;
            Optional<Player> optionalPlayer = this.server.getPlayer(request.player().getUniqueId());
            if (optionalPlayer.isEmpty()) return;
            RegisteredServer targetServer = optionalServer.get();
            Player targetPlayer = optionalPlayer.get();
            teleportRequests.put(targetPlayer.getUniqueId(), request);
            targetPlayer.createConnectionRequest(targetServer).connectWithIndication().whenComplete((result, throwable) -> {
                if (throwable != null) teleportRequests.remove(targetPlayer.getUniqueId());
                else if (!result) teleportRequests.remove(targetPlayer.getUniqueId());
            });
        }
    }

}
