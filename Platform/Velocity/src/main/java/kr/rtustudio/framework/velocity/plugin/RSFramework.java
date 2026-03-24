package kr.rtustudio.framework.velocity.plugin;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.velocity.VelocityProxium;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;

@Slf4j(topic = "RSFramework")
@SuppressWarnings("unused")
public class RSFramework {

    private final ProxyServer server;
    private final Path dir;
    private final Libraries libraries;

    private VelocityProxium proxium;
    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();

    @Inject
    public RSFramework(ProxyServer server, @DataDirectory Path dir) {
        this.server = server;
        this.dir = dir;
        this.libraries =
                new Libraries(
                        this,
                        log,
                        dir.getParent().resolve("RSFramework"),
                        server.getPluginManager());
    }

    @Subscribe
    private void onInitialize(ProxyInitializeEvent event) {
        // Utilities
        libraries.load("com.google.code.gson:gson:2.13.1");
        libraries.load("com.google.guava:guava:33.4.8-jre");
        libraries.load("org.apache.commons:commons-lang3:3.18.0");
        libraries.load("org.xerial.snappy:snappy-java:1.1.10.8");
        libraries.load("it.unimi.dsi:fastutil:8.5.18");

        // Netty
        libraries.load("io.netty:netty-buffer:4.1.111.Final");
        libraries.load("io.netty:netty-transport:4.1.111.Final");
        libraries.load("io.netty:netty-handler:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http:4.1.111.Final");
        libraries.load("io.netty:netty-codec-http2:4.1.111.Final");

        // Fory
        libraries.load(
                "org.apache.fory:fory-core:0.15.0", "org.apache.fory", "kr.rtustudio.bridge.fory");

        // BouncyCastle
        libraries.load("org.bouncycastle:bcprov-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcutil-jdk18on:1.80");
        libraries.load("org.bouncycastle:bcpkix-jdk18on:1.80");

        this.proxium = new VelocityProxium(server, dir.toAbsolutePath().getParent().getParent());
        this.server.getEventManager().register(this, proxium);

        registerTeleportRouting();

        log.info("RSFramework Velocity loaded.");
    }

    // ── 크로스 서버 텔레포트 라우팅 ──

    private void registerTeleportRouting() {
        proxium.subscribe(
                BridgeChannel.INTERNAL,
                TeleportRequest.class,
                this::handleTeleport);
    }

    private void handleTeleport(TeleportRequest request) {
        var targetServer = server.getServer(request.server()).orElse(null);
        if (targetServer == null) return;

        Player player = server.getPlayer(request.player().getUniqueId()).orElse(null);
        if (player == null) return;

        // 이미 대상 서버에 있으면 → 패킷만 직접 전달 (서버 이동 불필요)
        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current != null && current.getServerInfo().getName().equals(request.server())) {
            proxium.sendToServer(request.server(), BridgeChannel.INTERNAL, request);
            return;
        }

        // 다른 서버 → 서버 이동 후 패킷 전달
        teleportRequests.put(player.getUniqueId(), request);
        player.createConnectionRequest(targetServer)
                .connectWithIndication()
                .whenComplete(
                        (result, throwable) -> {
                            if (throwable != null || !result) {
                                teleportRequests.remove(player.getUniqueId());
                            }
                        });
    }

    @Subscribe
    private void onServerConnect(ServerPostConnectEvent e) {
        Player player = e.getPlayer();
        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current == null) return;

        String serverName = current.getServerInfo().getName();
        TeleportRequest tpr = teleportRequests.remove(player.getUniqueId());
        if (tpr != null && serverName.equals(tpr.server())) {
            proxium.sendToServer(serverName, BridgeChannel.INTERNAL, tpr);
        }
    }

    @Subscribe
    private void onKick(KickedFromServerEvent e) {
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    @Subscribe
    private void onQuit(DisconnectEvent e) {
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }
}

