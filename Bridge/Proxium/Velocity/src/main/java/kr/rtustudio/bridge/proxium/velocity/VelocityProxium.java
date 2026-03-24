package kr.rtustudio.bridge.proxium.velocity;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.*;
import kr.rtustudio.bridge.proxium.api.protocol.velocity.VelocityAuth;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.core.MutableProxyPlayer;
import kr.rtustudio.bridge.proxium.core.ProxiumProxy;
import kr.rtustudio.bridge.proxium.core.configuration.ProxiumConfig;
import kr.rtustudio.bridge.proxium.core.handler.ProxyConnectionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private final ProxyServer server;
    private final Path dir;
    private final Toml config;

    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();

    public VelocityProxium(ProxyServer server, Path dir) {
        this(server, dir, ProxiumConfig.load(dir.resolve("plugins/RSFramework")));
    }

    private VelocityProxium(ProxyServer server, Path dir, ProxiumConfig settings) {
        super(
                new BridgeOptions(VelocityProxium.class.getClassLoader()),
                dir.resolve("plugins/RSFramework"),
                settings);
        this.server = server;
        this.dir = dir;
        this.config = new Toml().read(new File(dir.toFile(), "velocity.toml"));

        register(
                BridgeChannel.INTERNAL,
                BridgeChannel.class,
                ProxiumNode.class,
                PlayerList.class,
                ServerList.class,
                TeleportRequest.class,
                PlayerEvent.class,
                MutableProxyPlayer.class,
                RequestPacket.class,
                ResponsePacket.class,
                BroadcastMessage.class);

        registerInternalSubscription();

        if (isModernProxy()) {
            log.info("Detected modern proxy");
            loadForwardingSecret();
        }

        start(settings);

        // 프록시 초기화 시 velocity.toml에 등록된 모든 서버에 연결 시도
        server.getAllServers()
                .forEach(
                        s -> {
                            ServerInfo info = s.getServerInfo();
                            if (info.getAddress() instanceof InetSocketAddress addr) {
                                registerServer(
                                        new ProxiumNode(
                                                info.getName(),
                                                addr.getHostString(),
                                                addr.getPort()));
                            }
                        });
    }

    @Override
    protected void configureProtocol(Protocol.Builder builder) {
        builder.setProxyHandler(() -> new ProxyConnectionHandler(this));
        if (isModernProxy()) {
            builder.setProxyAuthHandler(VelocityAuth::new);
        }
    }

    @Override
    protected void onChannelRegistered(BridgeChannel channel) {
        startChannelProtocol(channel);
    }

    @Override
    public String getName() {
        return "velocity";
    }

    @Override
    public void close() {
        super.close();
        teleportRequests.clear();
    }

    // ── 이벤트 핸들러 ──

    @Subscribe
    private void onProxyShutdown(ProxyShutdownEvent event) {
        close();
    }

    @Subscribe
    private void onRegistered(ServerRegisteredEvent event) {
        ServerInfo info = event.registeredServer().getServerInfo();
        if (info.getAddress() instanceof InetSocketAddress addr) {
            registerServer(new ProxiumNode(info.getName(), addr.getHostString(), addr.getPort()));
        }
    }

    @Subscribe
    private void onUnregistered(ServerUnregisteredEvent event) {
        ServerInfo info = event.unregisteredServer().getServerInfo();
        InetSocketAddress address = info.getAddress();
        unregisterServer(
                new ProxiumNode(info.getName(), address.getHostString(), address.getPort()));
    }

    // ── 플레이어 이벤트 ──

    @Subscribe
    private void onJoin(ServerPostConnectEvent e) {
        Player player = e.getPlayer();
        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current == null) return;

        String serverName = current.getServerInfo().getName();
        ProxiumNode serverNode = getNode(serverName);

        ProxyPlayer proxyPlayer = players.get(player.getUniqueId());
        PlayerEvent.Action action;

        if (proxyPlayer == null) {
            proxyPlayer =
                    new MutableProxyPlayer(
                            this, player.getUniqueId(), player.getUsername(), serverNode);
            players.put(player.getUniqueId(), proxyPlayer);
            action = PlayerEvent.Action.JOIN;
        } else {
            ((MutableProxyPlayer) proxyPlayer).setNode(serverNode);
            action = PlayerEvent.Action.SWITCH;
        }

        broadcastPlayerEvent(new PlayerEvent(action, proxyPlayer));

        TeleportRequest tpr = teleportRequests.remove(player.getUniqueId());
        if (tpr != null && serverName.equals(tpr.server())) {
            Connection connection = getConnection(current.getServerInfo().getAddress());
            if (connection != null) {
                connection.send(options.encode(BridgeChannel.INTERNAL, tpr));
            }
        }
    }

    @Subscribe
    private void onKick(KickedFromServerEvent e) {
        handlePlayerLeave(e.getPlayer().getUniqueId());
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    @Subscribe
    private void onQuit(DisconnectEvent e) {
        handlePlayerLeave(e.getPlayer().getUniqueId());
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    // ── 내부 로직 ──

    private void registerInternalSubscription() {
        subscribe(BridgeChannel.INTERNAL, TeleportRequest.class, this::handleTeleport);
    }

    private void handleTeleport(TeleportRequest request) {
        var targetServer = server.getServer(request.server()).orElse(null);
        if (targetServer == null) return;

        Player player = server.getPlayer(request.player().getUniqueId()).orElse(null);
        if (player == null) return;

        teleportRequests.put(player.getUniqueId(), request);
        player.createConnectionRequest(targetServer)
                .connectWithIndication()
                .whenComplete(
                        (result, throwable) -> {
                            if (throwable != null || !result) {
                                teleportRequests.remove(player.getUniqueId());
                                Connection target =
                                        getConnection(targetServer.getServerInfo().getAddress());
                                if (target != null) {
                                    target.send(options.encode(BridgeChannel.INTERNAL, request));
                                }
                            }
                        });
    }

    private void handlePlayerLeave(UUID uniqueId) {
        ProxyPlayer removed = players.remove(uniqueId);
        if (removed != null) {
            broadcastPlayerEvent(new PlayerEvent(PlayerEvent.Action.LEAVE, removed));
        }
    }

    private void broadcastPlayerEvent(PlayerEvent event) {
        byte[] frame = options.encode(BridgeChannel.INTERNAL, event);
        getConnectedServers().forEach(conn -> conn.send(frame));
    }

    private void loadForwardingSecret() {
        String envSecret = System.getenv("VELOCITY_FORWARDING_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            VelocityAuth.setSecret(envSecret.getBytes(StandardCharsets.UTF_8));
            return;
        }

        String secretPath = config.getString("forwarding-secret-file", "");
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
        String mode = config.getString("player-info-forwarding-mode", "");
        if (!List.of("modern", "bungeeguard").contains(mode.toLowerCase())) return false;

        String envSecret = System.getenv("VELOCITY_FORWARDING_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) return true;

        String secretPath = config.getString("forwarding-secret-file", "");
        if (secretPath.isEmpty()) return false;

        File file = new File(dir.toFile(), secretPath);
        if (!file.exists() || !file.isFile()) return false;

        try {
            return !String.join("", Files.readAllLines(file.toPath())).isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
}
