package kr.rtustudio.bridge.proxium.velocity;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.Broadcast;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.SendMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ServerName;
import kr.rtustudio.bridge.proxium.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.RegisteredServer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.api.proxy.request.teleport.LocationTeleport;
import kr.rtustudio.bridge.proxium.api.proxy.request.teleport.PlayerTeleport;
import kr.rtustudio.bridge.proxium.core.ProxiumProxy;
import kr.rtustudio.bridge.proxium.core.config.ProxiumSettings;
import kr.rtustudio.bridge.proxium.core.protocol.ServerPacketHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.velocitypowered.api.proxy.server.ServerInfo;

@Slf4j(topic = "Proxium")
@Getter
public class VelocityProxium extends ProxiumProxy {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ProxyServer proxyServer;
    private final Path dir;
    private final Toml velocityConfig;

    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();

    public VelocityProxium(ProxyServer server, Path dir) {
        this(server, dir, ProxiumSettings.load(dir.resolve("plugins/RSFramework")));
    }

    private VelocityProxium(ProxyServer server, Path dir, ProxiumSettings settings) {
        super(
                settings.toBridgeOptions(VelocityProxium.class.getClassLoader()),
                dir.resolve("plugins/RSFramework"),
                settings,
                toRegisteredServers(server));
        this.proxyServer = server;
        this.dir = dir;
        this.velocityConfig = new Toml().read(new File(dir.toFile(), "velocity.toml"));

        Protocol.Builder protocol = Protocol.create(BridgeChannel.INTERNAL);
        protocol.setOptions(options);
        protocol.setCompression(settings.getCompression());
        protocol.setMaxPacketSize(settings.getMaxPacketSize());

        options.register(
                BridgeChannel.INTERNAL,
                BridgeChannel.class,
                SendMessage.class,
                Broadcast.class,
                ServerName.class,
                PlayerList.class,
                LocationTeleport.class,
                PlayerTeleport.class);
        registeredChannels.add(BridgeChannel.INTERNAL);

        registerInternalSubscription();

        if (isModernProxy()) {
            log.info("Detected modern proxy");
            loadForwardingSecret();
            protocol.setProxyAuthHandler(VelocityAuth::new);
        }
        protocol.setProxyHandler(() -> new ConnectionHandler(this)).load();
    }

    private static List<RegisteredServer> toRegisteredServers(ProxyServer server) {
        return server.getAllServers().stream()
                .map(
                        s ->
                                new RegisteredServer(
                                        s.getServerInfo().getName(),
                                        s.getServerInfo().getAddress()))
                .toList();
    }

    @Override
    protected void onChannelRegistered(BridgeChannel channel) {
        loadChannelProtocol(channel);
    }

    @Override
    public void subscribe(BridgeChannel channel, Consumer<Object> handler) {
        super.subscribe(channel, handler);
        if (!registeredChannels.contains(channel)) {
            loadChannelProtocol(channel);
        }
    }

    @Override
    public String getServer() {
        return "Velocity Proxy";
    }

    @Override
    public void close() {
        super.close();
        teleportRequests.clear();
    }

    @Override
    protected void onServerConnected(Connection connection) {
        String motd = proxyServer.getConfiguration().getMotd().toString();
        connection.send(options.encode(BridgeChannel.INTERNAL, new ServerName(motd, "Velocity")));
    }

    @Override
    protected Object createStandaloneServerName() {
        return new ServerName("Standalone Server", "Velocity");
    }

    private void registerInternalSubscription() {
        subscribe(
                BridgeChannel.INTERNAL,
                packet -> {
                    if (packet instanceof TeleportRequest request) {
                        handleTeleport(request);
                    } else if (packet instanceof Broadcast broadcast) {
                        for (Player player : proxyServer.getAllPlayers()) {
                            player.sendMessage(MINI_MESSAGE.deserialize(broadcast.minimessage()));
                        }
                    } else if (packet instanceof SendMessage message) {
                        proxyServer
                                .getPlayer(message.player().uniqueId())
                                .ifPresent(
                                        player ->
                                                player.sendMessage(
                                                        MINI_MESSAGE.deserialize(
                                                                message.minimessage())));
                    }
                });
    }

    private void handleTeleport(TeleportRequest request) {
        var targetServer = proxyServer.getServer(request.server()).orElse(null);
        if (targetServer == null) return;

        Player player = proxyServer.getPlayer(request.player().uniqueId()).orElse(null);
        if (player == null) return;

        teleportRequests.put(player.getUniqueId(), request);
        player.createConnectionRequest(targetServer)
                .connectWithIndication()
                .whenComplete(
                        (result, throwable) -> {
                            if (throwable != null || !result) {
                                teleportRequests.remove(player.getUniqueId());
                                Connection target =
                                        ConnectionHandler.getServer(
                                                targetServer.getServerInfo().getAddress());
                                if (target != null) {
                                    target.send(options.encode(BridgeChannel.INTERNAL, request));
                                }
                            }
                        });
    }

    private void loadChannelProtocol(BridgeChannel channel) {
        Protocol.Builder protocol = Protocol.create(channel);
        protocol.setCompression(getSettings().getCompression());
        protocol.setMaxPacketSize(getSettings().getMaxPacketSize());

        if (isModernProxy()) {
            protocol.setProxyAuthHandler(VelocityAuth::new);
        }

        protocol.setProxyHandler(ServerPacketHandler::new).load();
    }

    private void loadForwardingSecret() {
        String secretPath = velocityConfig.getString("forwarding-secret-file", "");
        if (secretPath.isEmpty()) return;

        File file = new File(dir.toFile(), secretPath);
        if (!file.exists() || !file.isFile()) return;

        try {
            String content = String.join("", Files.readAllLines(file.toPath()));
            if (!content.isEmpty()) {
                VelocityAuth.setSecret(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("Failed to read forwarding secret", e);
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
        registerServer(new RegisteredServer(info.getName(), info.getAddress()));
    }

    @Subscribe
    public void onUnregister(ServerUnregisteredEvent event) {
        ServerInfo info = event.unregisteredServer().getServerInfo();
        unregisterServer(new RegisteredServer(info.getName(), info.getAddress()));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        close();
    }

    @Subscribe
    private void onJoin(ServerPostConnectEvent e) {
        updatePlayerList();

        Player player = e.getPlayer();
        player.getCurrentServer()
                .map(ServerConnection::getServerInfo)
                .map(info -> ConnectionHandler.getServer(info.getAddress()))
                .ifPresent(
                        connection -> {
                            TeleportRequest tpr = teleportRequests.remove(player.getUniqueId());
                            if (tpr != null) {
                                connection.send(options.encode(BridgeChannel.INTERNAL, tpr));
                            }
                        });
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
        for (Player player : proxyServer.getAllPlayers()) {
            ServerConnection server = player.getCurrentServer().orElse(null);
            if (server == null) continue;
            Locale locale = Objects.requireNonNullElse(player.getEffectiveLocale(), Locale.US);
            String serverName = server.getServerInfo().getName();
            proxyPlayers.put(
                    player.getUniqueId(),
                    new ProxyPlayer(
                            player.getUniqueId(), player.getUsername(), locale, serverName));
        }
        byte[] listFrame = options.encode(BridgeChannel.INTERNAL, new PlayerList(proxyPlayers));
        ConnectionHandler.getServers().forEach(conn -> conn.send(listFrame));
    }

    @Override
    public List<RegisteredServer> getRegisteredServers() {
        return toRegisteredServers(proxyServer);
    }
}
