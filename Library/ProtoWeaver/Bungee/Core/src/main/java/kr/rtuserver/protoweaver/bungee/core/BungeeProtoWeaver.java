package kr.rtuserver.protoweaver.bungee.core;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.CompressionType;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import kr.rtuserver.protoweaver.api.protocol.internal.*;
import kr.rtuserver.protoweaver.api.proxy.ProtoServer;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtuserver.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtuserver.protoweaver.api.proxy.request.teleport.PlayerTeleport;
import kr.rtuserver.protoweaver.api.serializer.CustomPacketSerializer;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import kr.rtuserver.protoweaver.core.protocol.protoweaver.ServerPacketHandler;
import kr.rtuserver.protoweaver.core.proxy.ProtoProxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.event.EventHandler;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class BungeeProtoWeaver implements kr.rtuserver.protoweaver.bungee.api.BungeeProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;

    private final Path dir;

    private final ProtoProxy protoProxy;
    private final ConcurrentHashMap<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();
    private final HandlerCallback callable = new HandlerCallback(this::onReady, this::onPacket);

    public BungeeProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);
        protocol = Protocol.create("rsframework", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Packet.class);
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(SendMessage.class);
        protocol.addPacket(Broadcast.class);
        protocol.addPacket(ServerName.class);
        protocol.addPacket(PlayerList.class);
        protocol.addPacket(LocationTeleport.class);
        protocol.addPacket(PlayerTeleport.class);

        protocol.setClientHandler(BungeeProtoHandler.class, callable).load();
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
        if (callback == null) protocol.setClientHandler(protocolHandler).load();
        else protocol.setClientHandler(protocolHandler, callback).load();
    }

    public void disable() {
        BungeeProtoHandler.getServers().forEach(server -> server.send(new ServerName("Standalone Server")));
        protoProxy.shutdown();
    }

    @Override
    public List<ProtoServer> getServers() {
        return server.getServersCopy().values().stream().map(server -> new ProtoServer(server.getName(), server.getSocketAddress())).toList();
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

    @EventHandler
    private void onJoin(ServerConnectedEvent e) {
        updatePlayerList();
        ProxiedPlayer player = e.getPlayer();
        Server server = player.getServer();
        if (server == null) return;
        ProtoConnection connection = BungeeProtoHandler.getServer(server.getSocketAddress());
        if (connection == null) return;
        TeleportRequest tpr = teleportRequests.get(player.getUniqueId());
        if (tpr != null) {
            connection.send(tpr);
            teleportRequests.remove(player.getUniqueId());
        }
    }

    @EventHandler
    private void onQuit(ServerDisconnectEvent e) {
        updatePlayerList();
    }

    @EventHandler
    private void onKick(ServerKickEvent e) {
        updatePlayerList();
    }

    private void updatePlayerList() {
        Map<UUID, ProxyPlayer> players = new HashMap<>();
        for (ProxiedPlayer player : server.getPlayers()) {
            Server server = player.getServer();
            if (server == null) continue;
            ServerInfo info = server.getInfo();
            UUID uuid = player.getUniqueId();
            ProxyPlayer pp = new ProxyPlayer(uuid, player.getName(), player.getLocale(), info.getName());
            players.put(uuid, pp);
        }
        BungeeProtoHandler.getServers().forEach(server -> server.send(new PlayerList(players)));
    }

    private void onReady(HandlerCallback.Ready data) {
        ProtoConnection connection = data.protoConnection();
        if (!connection.isOpen()) return;
        Map<String, ServerInfo> servers = this.server.getServersCopy();
        for (String name : servers.keySet()) {
            ServerInfo info = servers.get(name);
            if (connection.getRemoteAddress().equals(info.getSocketAddress())) {
                connection.send(new ServerName(name));
            }
        }
    }

    private void onPacket(HandlerCallback.Packet data) {
        Object packet = data.packet();
        if (packet instanceof ProtocolRegister(String namespace, String key, Set<Packet> packets)) {
            registerProtocol(namespace, key, packets, ServerPacketHandler.class, null);
        } else if (packet instanceof TeleportRequest request) {
            ServerInfo info = this.server.getServerInfo(request.server());
            if (info == null) return;
            ProxiedPlayer player = this.server.getPlayer(request.player().uniqueId());
            if (player == null) return;
            player.connect(info, (result, error) -> {
                if (error != null) teleportRequests.remove(player.getUniqueId());
                else if (!result) teleportRequests.remove(player.getUniqueId());
            });
        }
    }

}
