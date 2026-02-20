package kr.rtustudio.broker.protoweaver.velocity.core;

import kr.rtustudio.broker.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.broker.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.broker.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.broker.protoweaver.api.protocol.CompressionType;
import kr.rtustudio.broker.protoweaver.api.protocol.Packet;
import kr.rtustudio.broker.protoweaver.api.protocol.Protocol;
import kr.rtustudio.broker.protoweaver.api.protocol.internal.*;
import kr.rtustudio.broker.protoweaver.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.broker.protoweaver.api.proxy.ProtoServer;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyBroker;
import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.broker.protoweaver.api.proxy.request.TeleportRequest;
import kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.broker.protoweaver.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.broker.protoweaver.api.serializer.CustomPacketSerializer;
import kr.rtustudio.broker.protoweaver.api.serializer.ProtoSerializer;
import kr.rtustudio.broker.protoweaver.api.util.ProtoLogger;
import kr.rtustudio.broker.protoweaver.core.protocol.protoweaver.ServerPacketHandler;
import kr.rtustudio.broker.protoweaver.core.proxy.ProtoProxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

@Slf4j(topic = "RSF/ProtoWeaver")
@Getter
public class ProtoWeaver implements kr.rtustudio.broker.protoweaver.velocity.api.ProtoWeaver {

    private final ProxyServer server;
    private final Toml velocityConfig;
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
        this.velocityConfig = new Toml().read(new File(dir.toFile(), "velocity.toml"));

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

        if (isModernProxy()) {
            info("Detected modern proxy");
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setClientHandler(ProtoHandler.class, callable).load();
    }

    @Override
    public void register(String channel, Consumer<ProxyBroker.PacketRegistrar> registrar) {
        Set<Packet> packets = new HashSet<>();
        registrar.accept(
                new ProxyBroker.PacketRegistrar() {
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
        if (isModernProxy()) {
            protocol.setClientAuthHandler(VelocityAuth.class);
        }

        if (callback == null) {
            protocol.setClientHandler(protocolHandler).load();
        } else {
            protocol.setClientHandler(protocolHandler, callback).load();
        }
    }

    private boolean isModernProxy() {
        String mode = velocityConfig.getString("player-info-forwarding-mode", "");
        if (!List.of("modern", "bungeeguard").contains(mode.toLowerCase())) return false;

        String secretPath = velocityConfig.getString("forwarding-secret-file", "");
        if (secretPath.isEmpty()) return false;

        File file = new File(dir.toFile(), secretPath);
        if (!file.exists() || !file.isFile()) return false;

        try {
            return !String.join("", Files.readAllLines(file.toPath())).isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    @Subscribe
    public void onRegister(ServerRegisteredEvent event) {
        ServerInfo info = event.registeredServer().getServerInfo();
        protoProxy.register(new ProtoServer(info.getName(), info.getAddress()));
    }

    @Subscribe
    public void onUnregister(ServerUnregisteredEvent event) {
        ServerInfo info = event.unregisteredServer().getServerInfo();
        protoProxy.unregister(new ProtoServer(info.getName(), info.getAddress()));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        shutdown();
    }

    @Subscribe
    private void onJoin(ServerPostConnectEvent e) {
        updatePlayerList();

        Player player = e.getPlayer();
        Optional<ServerConnection> scOptional = player.getCurrentServer();
        if (scOptional.isEmpty()) return;

        ServerInfo info = scOptional.get().getServerInfo();
        ProtoConnection connection = ProtoHandler.getServer(info.getAddress());
        if (connection == null) return;

        TeleportRequest tpr = teleportRequests.remove(player.getUniqueId());
        if (tpr != null) {
            connection.send(tpr);
        }
    }

    @Subscribe
    private void onKick(KickedFromServerEvent e) {
        updatePlayerList();
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    @Subscribe
    private void onQuit(DisconnectEvent e) {
        updatePlayerList();
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    private void updatePlayerList() {
        Map<UUID, ProxyPlayer> proxyPlayers = new HashMap<>();
        for (Player player : server.getAllPlayers()) {
            Optional<ServerConnection> connection = player.getCurrentServer();
            if (connection.isEmpty()) continue;

            ServerInfo info = connection.get().getServerInfo();
            proxyPlayers.put(
                    player.getUniqueId(),
                    new ProxyPlayer(
                            player.getUniqueId(),
                            player.getUsername(),
                            player.getEffectiveLocale() == null
                                    ? Locale.US
                                    : player.getEffectiveLocale(),
                            info.getName()));
        }

        PlayerList listPacket = new PlayerList(proxyPlayers);
        ProtoHandler.getServers().forEach(conn -> conn.send(listPacket));
    }

    @Override
    public List<ProtoServer> getServers() {
        return server.getAllServers().stream()
                .map(
                        s ->
                                new ProtoServer(
                                        s.getServerInfo().getName(),
                                        s.getServerInfo().getAddress()))
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

    private void onReady(HandlerCallback.Ready data) {
        ProtoConnection connection = data.protoConnection();
        if (!connection.isOpen()) return;

        for (RegisteredServer rs : this.server.getAllServers()) {
            ServerInfo info = rs.getServerInfo();
            if (connection.getRemoteAddress().equals(info.getAddress())) {
                connection.send(new ServerName(info.getName()));
            }
        }
    }

    private void onPacket(HandlerCallback.Packet data) {
        Object packet = data.packet();

        if (packet instanceof ProtocolRegister(String channel, Set<Packet> packets)) {
            loadChannelProtocol(channel, packets, ServerPacketHandler.class, null);
        } else if (packet instanceof TeleportRequest request) {
            Optional<RegisteredServer> optServer = this.server.getServer(request.server());
            if (optServer.isEmpty()) return;

            Optional<Player> optPlayer = this.server.getPlayer(request.player().uniqueId());
            if (optPlayer.isEmpty()) return;

            Player targetPlayer = optPlayer.get();
            teleportRequests.put(targetPlayer.getUniqueId(), request);

            targetPlayer
                    .createConnectionRequest(optServer.get())
                    .connectWithIndication()
                    .whenComplete(
                            (result, throwable) -> {
                                if (throwable != null || !result) {
                                    teleportRequests.remove(targetPlayer.getUniqueId());
                                }
                            });
        }
    }
}
