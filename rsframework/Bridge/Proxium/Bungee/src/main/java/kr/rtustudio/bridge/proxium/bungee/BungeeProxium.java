package kr.rtustudio.bridge.proxium.bungee;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerEvent;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerList;
import kr.rtustudio.bridge.proxium.api.protocol.internal.RequestPacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ResponsePacket;
import kr.rtustudio.bridge.proxium.api.protocol.internal.TransactionPacket;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.core.ProxiumProxy;
import kr.rtustudio.bridge.proxium.core.configuration.ProxiumConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
import java.util.stream.Collectors;

@Slf4j(topic = "Proxium")
@Getter
public class BungeeProxium extends ProxiumProxy implements Listener {

    private final ProxyServer proxyServer;

    private final Map<UUID, TeleportRequest> teleportRequests = new ConcurrentHashMap<>();

    public BungeeProxium(ProxyServer server, Path dataFolder) {
        this(server, dataFolder, ProxiumConfig.load(dataFolder));
    }

    private BungeeProxium(ProxyServer server, Path dataFolder, ProxiumConfig settings) {
        super(settings.toBridgeOptions(BungeeProxium.class.getClassLoader()), dataFolder, settings);
        this.proxyServer = server;

        Protocol.Builder protocol = Protocol.create(BridgeChannel.INTERNAL);
        protocol.setOptions(options);
        protocol.setCompression(settings.getCompression());
        protocol.setMaxPacketSize(settings.getMaxPacketSize());

        options.register(
                BridgeChannel.INTERNAL,
                BridgeChannel.class,
                ProxiumNode.class,
                PlayerList.class,
                TeleportRequest.class,
                PlayerEvent.class,
                RequestPacket.class,
                ResponsePacket.class);
        registeredChannels.add(BridgeChannel.INTERNAL);

        registerInternalSubscription();

        protocol.setProxyHandler(() -> new ConnectionHandler(this)).load();

        getProxiumNodes().forEach(this::registerServer);
    }

    private List<ProxiumNode> toProxiumNodes(ProxyServer server) {
        return proxyServer.getServersCopy().values().stream()
                .map(s -> new ProxiumNode(s.getName(), s.getSocketAddress().toString()))
                .collect(Collectors.toList());
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
            ProxiumNode node = new ProxiumNode(entry.getKey(), addr.toString());
            registerServer(node);
            connection.send(options.encode(BridgeChannel.INTERNAL, node));

            if (!players.isEmpty()) {
                connection.send(options.encode(BridgeChannel.INTERNAL, new PlayerList(players)));
            }
            return;
        }
    }

    // TODO: fix minimessage for bungee
    private void registerInternalSubscription() {
        subscribe(
                BridgeChannel.INTERNAL,
                packet -> {
                    if (packet instanceof TeleportRequest request) {
                        handleTeleport(request);
                    } else if (packet instanceof TransactionPacket) {
                        routeBridgePacket(packet);
                    }
                });
    }

    private void handleTeleport(TeleportRequest request) {
        ServerInfo info = this.proxyServer.getServerInfo(request.server());
        if (info == null) return;

        ProxiedPlayer player = this.proxyServer.getPlayer(request.player().getUniqueId());
        if (player == null) return;

        teleportRequests.put(player.getUniqueId(), request);
        player.connect(
                info,
                (result, error) -> {
                    if (error != null || !result) {
                        teleportRequests.remove(player.getUniqueId());
                        Connection target = getConnection(info.getSocketAddress());
                        if (target != null)
                            target.send(options.encode(BridgeChannel.INTERNAL, request));
                    }
                });
    }

    private void loadChannelProtocol(BridgeChannel channel) {
        Protocol.Builder protocol = Protocol.create(channel);
        protocol.setCompression(getSettings().getCompression());
        protocol.setMaxPacketSize(getSettings().getMaxPacketSize());
        protocol.setProxyHandler(() -> new ConnectionHandler(this)).load();
    }

    @Override
    public List<ProxiumNode> getProxiumNodes() {
        return toProxiumNodes(proxyServer);
    }

    @EventHandler
    private void onJoin(ServerConnectedEvent e) {
        ProxiedPlayer bungeePlayer = e.getPlayer();
        if (bungeePlayer.getServer() == null) return;

        String serverName = e.getServer().getInfo().getName();
        Locale locale = Objects.requireNonNullElse(bungeePlayer.getLocale(), Locale.US);

        ProxyPlayer proxyPlayer = players.get(bungeePlayer.getUniqueId());
        PlayerEvent.Action action;

        if (proxyPlayer == null) {
            proxyPlayer =
                    new ProxyPlayer(
                            this,
                            bungeePlayer.getUniqueId(),
                            bungeePlayer.getName(),
                            locale,
                            serverName);
            players.put(bungeePlayer.getUniqueId(), proxyPlayer);
            action = PlayerEvent.Action.JOIN;
        } else {
            proxyPlayer.setServer(serverName);
            proxyPlayer.setLocale(locale);
            action = PlayerEvent.Action.SWITCH;
        }

        broadcastPlayerEvent(new PlayerEvent(action, proxyPlayer));

        Connection conn = getConnection(e.getServer().getInfo().getSocketAddress());
        if (conn != null) {
            TeleportRequest tpr = teleportRequests.remove(bungeePlayer.getUniqueId());
            if (tpr != null) {
                conn.send(options.encode(BridgeChannel.INTERNAL, tpr));
            }
        }
    }

    @EventHandler
    private void onServerDisconnect(ServerDisconnectEvent e) {
        handlePlayerLeave(e.getPlayer().getUniqueId());
        teleportRequests.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onServerKick(ServerKickEvent e) {
        handlePlayerLeave(e.getPlayer().getUniqueId());
        teleportRequests.remove(e.getPlayer().getUniqueId());
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
}
