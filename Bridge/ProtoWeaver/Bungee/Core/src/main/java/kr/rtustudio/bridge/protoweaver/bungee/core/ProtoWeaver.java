package kr.rtustudio.bridge.protoweaver.bungee.core;

import kr.rtustudio.bridge.BridgeChannel;
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

    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();
    private final Map<BridgeChannel, Consumer<Object>> channelHandlers = new ConcurrentHashMap<>();

    public ProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);

        Protocol.Builder protocol = Protocol.create(BridgeChannel.INTERNAL);
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

        protocol.setClientHandler(ProtoHandler.class, this).load();
    }

    @Override
    public void register(BridgeChannel channel, Consumer<ProxyBridge.PacketRegistrar> registrar) {
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
        loadChannelProtocol(channel, packets, ServerPacketHandler.class);
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        channelHandlers.put(channel, handler);
    }

    @Override
    public void publish(BridgeChannel channel, Object message) {
        ProtoHandler.getServers().forEach(conn -> conn.send(message));
    }

    @Override
    public void unsubscribe(BridgeChannel channel) {
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

    @Override
    public void ready(ProtoConnection connection) {
        if (!connection.isOpen()) return;

        server.getServersCopy()
                .forEach(
                        (name, info) -> {
                            if (connection.getRemoteAddress().equals(info.getSocketAddress())) {
                                connection.send(new ServerName(name));
                            }
                        });

        Consumer<Object> systemHandler =
                channelHandlers.get(BridgeChannel.of("rsf:system:connection"));
        if (systemHandler != null) {
            systemHandler.accept(connection);
        }

        subscribe(
                BridgeChannel.INTERNAL,
                packet -> {
                    if (packet
                            instanceof
                            ProtocolRegister(BridgeChannel channel, Set<Packet> packets)) {
                        loadChannelProtocol(channel, packets, ServerPacketHandler.class);
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
                    } else if (packet instanceof Broadcast broadcast) {
                        for (ProxiedPlayer player : server.getPlayers()) {
                            kr.rtustudio.bridge.protoweaver.bungee.api.ProtoWeaver.sendMessage(
                                    player, broadcast.minimessage());
                        }
                    } else if (packet instanceof SendMessage message) {
                        ProxiedPlayer player = server.getPlayer(message.player().uniqueId());
                        if (player != null) {
                            kr.rtustudio.bridge.protoweaver.bungee.api.ProtoWeaver.sendMessage(
                                    player, message.minimessage());
                        }
                    }
                });
    }

    private void loadChannelProtocol(
            BridgeChannel channel,
            Set<Packet> packets,
            Class<? extends kr.rtustudio.bridge.protoweaver.api.ProtoConnectionHandler>
                    protocolHandler) {

        Protocol.Builder protocol = Protocol.create(channel);
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

        protocol.setClientHandler(protocolHandler).load();
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
}
