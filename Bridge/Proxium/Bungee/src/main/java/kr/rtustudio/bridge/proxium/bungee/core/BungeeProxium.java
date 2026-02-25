package kr.rtustudio.bridge.proxium.bungee.core;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.Broadcast;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.SendMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ServerName;
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
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j(topic = "Proxium")
@Getter
public class BungeeProxium extends ProxiumProxy implements Listener {

    private final ProxyServer proxyServer;

    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();

    public BungeeProxium(ProxyServer server, Path dataFolder) {
        this(server, dataFolder, ProxiumSettings.load(dataFolder));
    }

    private BungeeProxium(ProxyServer server, Path dataFolder, ProxiumSettings settings) {
        super(
                settings.toBridgeOptions(BungeeProxium.class.getClassLoader()),
                dataFolder,
                settings,
                toRegisteredServers(server));
        this.proxyServer = server;

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

        protocol.setProxyHandler(() -> new ConnectionHandler(this)).load();
    }

    private static String addressKey(InetSocketAddress address) {
        String host =
                (address.getAddress() != null)
                        ? address.getAddress().getHostAddress()
                        : address.getHostString();
        if ("localhost".equalsIgnoreCase(host)) {
            host = "127.0.0.1";
        }
        return host + ":" + address.getPort();
    }

    private static List<RegisteredServer> toRegisteredServers(ProxyServer server) {
        return server.getServersCopy().values().stream()
                .map(s -> new RegisteredServer(s.getName(), s.getSocketAddress()))
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
        return "BungeeCord Proxy";
    }

    @Override
    public void close() {
        super.close();
        teleportRequests.clear();
    }

    @Override
    protected void onServerConnected(Connection connection) {
        String remoteAddress = addressKey(connection.getRemoteAddress());
        for (var entry : proxyServer.getServersCopy().entrySet()) {
            if (!(entry.getValue().getSocketAddress() instanceof InetSocketAddress addr)) continue;
            if (!remoteAddress.equals(addressKey(addr))) continue;
            connection.send(
                    options.encode(
                            BridgeChannel.INTERNAL, new ServerName(entry.getKey(), "BungeeCord")));
            return;
        }
    }

    @Override
    protected Object createStandaloneServerName() {
        return new ServerName("Standalone Server", "BungeeCord");
    }

    // TODO: fix minimessage for bungee
    private void registerInternalSubscription() {
        subscribe(
                BridgeChannel.INTERNAL,
                packet -> {
                    if (packet instanceof TeleportRequest request) {
                        handleTeleport(request);
                    } else if (packet instanceof Broadcast broadcast) {
                        for (ProxiedPlayer player : proxyServer.getPlayers()) {
                            player.sendMessage(
                                    TextComponent.fromLegacyText(broadcast.minimessage()));
                        }
                    } else if (packet instanceof SendMessage message) {
                        ProxiedPlayer player = proxyServer.getPlayer(message.player().uniqueId());
                        if (player != null) {
                            player.sendMessage(TextComponent.fromLegacyText(message.minimessage()));
                        }
                    }
                });
    }

    private void handleTeleport(TeleportRequest request) {
        ServerInfo info = this.proxyServer.getServerInfo(request.server());
        if (info == null) return;

        ProxiedPlayer player = this.proxyServer.getPlayer(request.player().uniqueId());
        if (player == null) return;

        teleportRequests.put(player.getUniqueId(), request);
        player.connect(
                info,
                (result, error) -> {
                    if (error != null || !result) {
                        teleportRequests.remove(player.getUniqueId());
                        Connection target = ConnectionHandler.getServer(info.getSocketAddress());
                        if (target != null)
                            target.send(options.encode(BridgeChannel.INTERNAL, request));
                    }
                });
    }

    private void loadChannelProtocol(BridgeChannel channel) {
        Protocol.Builder protocol = Protocol.create(channel);
        protocol.setCompression(getSettings().getCompression());
        protocol.setMaxPacketSize(getSettings().getMaxPacketSize());
        protocol.setProxyHandler(ServerPacketHandler::new).load();
    }

    @Override
    public List<RegisteredServer> getRegisteredServers() {
        return toRegisteredServers(proxyServer);
    }

    @EventHandler
    private void onJoin(ServerConnectedEvent e) {
        updatePlayerList();

        ProxiedPlayer bungeePlayer = e.getPlayer();
        Optional.ofNullable(bungeePlayer.getServer())
                .map(Server::getInfo)
                .map(info -> ConnectionHandler.getServer(info.getSocketAddress()))
                .ifPresent(
                        conn -> {
                            TeleportRequest tpr =
                                    teleportRequests.remove(bungeePlayer.getUniqueId());
                            if (tpr != null) {
                                conn.send(options.encode(BridgeChannel.INTERNAL, tpr));
                            }
                        });
    }

    @EventHandler
    private void onServerDisconnect(ServerDisconnectEvent e) {
        updatePlayerList();
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onServerKick(ServerKickEvent e) {
        updatePlayerList();
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    private void updatePlayerList() {
        Map<UUID, ProxyPlayer> proxyPlayers = new HashMap<>();
        for (ProxiedPlayer p : proxyServer.getPlayers()) {
            if (p.getServer() == null) continue;
            Locale locale = Objects.requireNonNullElse(p.getLocale(), Locale.US);
            String serverName = p.getServer().getInfo().getName();
            proxyPlayers.put(
                    p.getUniqueId(),
                    new ProxyPlayer(p.getUniqueId(), p.getName(), locale, serverName));
        }
        byte[] frame = options.encode(BridgeChannel.INTERNAL, new PlayerList(proxyPlayers));
        ConnectionHandler.getServers().forEach(conn -> conn.send(frame));
    }
}
