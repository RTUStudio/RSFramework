package kr.rtustudio.bridge.protoweaver.bungee.core;

import kr.rtustudio.bridge.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.bridge.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.bridge.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.bridge.protoweaver.api.protocol.CompressionType;
import kr.rtustudio.bridge.protoweaver.api.protocol.Packet;
import kr.rtustudio.bridge.protoweaver.api.protocol.Protocol;
import kr.rtustudio.bridge.protoweaver.api.protocol.internal.*;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProtoServer;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyBridge;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.bridge.protoweaver.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.bridge.protoweaver.api.serializer.CustomPacketSerializer;
import kr.rtustudio.bridge.protoweaver.api.serializer.ProtoSerializer;
import kr.rtustudio.bridge.protoweaver.api.util.ProtoLogger;
import kr.rtustudio.bridge.protoweaver.core.protocol.protoweaver.ServerPacketHandler;
import kr.rtustudio.bridge.protoweaver.core.proxy.ProtoProxy;
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
import java.util.function.Consumer;

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class ProtoWeaver implements kr.rtustudio.bridge.protoweaver.bungee.api.ProtoWeaver {

    private final ProxyServer server;
    private final Path dir;

    private final ProtoProxy protoProxy;
    private final HandlerCallback callable = new HandlerCallback(this::onReady, this::onPacket);

    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Object>> channelHandlers = new ConcurrentHashMap<>();

    public ProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);

        Protocol.Builder protocol = Protocol.create("rsframework", "internal");
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

        protocol.setClientHandler(ProtoHandler.class, callable).load();
    }

    @Override
    public void register(String channel, Consumer<ProxyBridge.PacketRegistrar> registrar) {
        Set<Packet> packets = new HashSet<>();
        registrar.accept(
                new ProxyBridge.PacketRegistrar() {
                    @Override
                    public void register(Class<?> type) {
                        packets.add(Packet.of(type));
                    }

                    @Override
                    public void register(
                            Class<?> type, Class<? extends ProtoSerializer<?>> serializer) {
                        packets.add(Packet.of(type, serializer));
                    }
                });
        loadChannelProtocol(channel, packets, ServerPacketHandler.class, null);
    }

    @Override
    public void subscribe(String channel, Consumer<Object> handler) {
        channelHandlers.put(channel, handler);
    }

    @Override
    public void publish(String channel, Object message) {
        ProtoHandler.getServers().forEach(conn -> conn.send(message));
    }

    @Override
    public void unsubscribe(String channel) {
        channelHandlers.remove(channel);
    }

    @Override
    public void shutdown() {
        ServerName standalone = new ServerName("Standalone Server");
        ProtoHandler.getServers().forEach(conn -> conn.send(standalone));
        protoProxy.shutdown();
        teleportRequests.clear();
        channelHandlers.clear();
    }

    private void loadChannelProtocol(
            String channel,
            Set<Packet> packets,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback) {
        String[] parts = channel.split(":", 2);
        String namespace = parts[0];
        String key = parts.length > 1 ? parts[1] : parts[0];

        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb

        for (Packet packet : packets) {
            if (packet.getSerializer() != null) {
                protocol.addPacket(packet.getPacket(), packet.getSerializer());
            } else {
                protocol.addPacket(packet.getPacket());
            }
        }

        protocol.addPacket(CustomPacket.class, CustomPacketSerializer.class);

        if (callback == null) {
            protocol.setClientHandler(protocolHandler).load();
        } else {
            protocol.setClientHandler(protocolHandler, callback).load();
        }
    }

    @Override
    public List<ProtoServer> getServers() {
        return server.getServersCopy().values().stream()
                .map(s -> new ProtoServer(s.getName(), s.getSocketAddress()))
                .toList();
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
        Server playerServer = player.getServer();
        if (playerServer == null) return;

        ProtoConnection connection = ProtoHandler.getServer(playerServer.getSocketAddress());
        if (connection == null) return;

        TeleportRequest tpr = teleportRequests.remove(player.getUniqueId());
        if (tpr != null) {
            connection.send(tpr);
        }
    }

    @EventHandler
    private void onQuit(ServerDisconnectEvent e) {
        updatePlayerList();
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onKick(ServerKickEvent e) {
        updatePlayerList();
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    private void updatePlayerList() {
        Map<UUID, ProxyPlayer> proxyPlayers = new HashMap<>();
        for (ProxiedPlayer player : server.getPlayers()) {
            Server playerServer = player.getServer();
            if (playerServer == null) continue;

            ServerInfo info = playerServer.getInfo();
            proxyPlayers.put(
                    player.getUniqueId(),
                    new ProxyPlayer(
                            player.getUniqueId(),
                            player.getName(),
                            player.getLocale() == null ? Locale.US : player.getLocale(),
                            info.getName()));
        }

        PlayerList listPacket = new PlayerList(proxyPlayers);
        ProtoHandler.getServers().forEach(conn -> conn.send(listPacket));
    }

    private void onReady(HandlerCallback.Ready data) {
        ProtoConnection connection = data.protoConnection();
        if (!connection.isOpen()) return;

        server.getServersCopy()
                .forEach(
                        (name, info) -> {
                            if (connection.getRemoteAddress().equals(info.getSocketAddress())) {
                                connection.send(new ServerName(name));
                            }
                        });
    }

    private void onPacket(HandlerCallback.Packet data) {
        Object packet = data.packet();

        if (packet instanceof ProtocolRegister(String channel, Set<Packet> packets)) {
            loadChannelProtocol(channel, packets, ServerPacketHandler.class, null);
        } else if (packet instanceof TeleportRequest request) {
            ServerInfo info = this.server.getServerInfo(request.server());
            if (info == null) return;

            ProxiedPlayer player = this.server.getPlayer(request.player().uniqueId());
            if (player == null) return;

            teleportRequests.put(player.getUniqueId(), request);
            player.connect(
                    info,
                    (result, error) -> {
                        if (error != null || !result) {
                            teleportRequests.remove(player.getUniqueId());
                        }
                    });
        }
    }
}
