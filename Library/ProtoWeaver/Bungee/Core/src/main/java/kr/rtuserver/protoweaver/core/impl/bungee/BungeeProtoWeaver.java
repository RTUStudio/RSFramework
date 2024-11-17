package kr.rtuserver.protoweaver.core.impl.bungee;

import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.ProxyPlayer;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.impl.bungee.BungeeProtoHandler;
import kr.rtuserver.protoweaver.api.protocol.CompressionType;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.Protocol;
import kr.rtuserver.protoweaver.api.protocol.internal.*;
import kr.rtuserver.protoweaver.api.proxy.ProtoServer;
import kr.rtuserver.protoweaver.api.util.ProtoLogger;
import kr.rtuserver.protoweaver.core.protocol.protoweaver.CommonPacketHandler;
import kr.rtuserver.protoweaver.core.proxy.ProtoProxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j(topic = "RSFramework/ProtoWeaver")
@Getter
public class BungeeProtoWeaver implements kr.rtuserver.protoweaver.api.impl.bungee.BungeeProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;

    private final Path dir;

    private final ProtoProxy protoProxy;
    private final HandlerCallback callable = new HandlerCallback(null, this::onPacket);

    public BungeeProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);
        protocol = Protocol.create("rsframework", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(Packet.class);
        protocol.addPacket(PlayerList.class);
        protocol.addPacket(Packet.of(BroadcastChat.class, true, true));
        protocol.addPacket(ProxyPlayer.class);
        protocol.addPacket(Packet.of(StorageSync.class, true, true));
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
            if (packet.isBothSide()) protocol.addPacket(packet);
            else protocol.addPacket(Packet.of(CustomPacket.class, packet.isGlobal(), false));
        }
        protocol.setClientHandler(CommonPacketHandler.class, null).load();
    }

    public void disable() {
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
    private void onJoin(PostLoginEvent e) {
        updatePlayerList();
    }

    @EventHandler
    private void onQuit(PlayerDisconnectEvent e) {
        updatePlayerList();
    }

    private void updatePlayerList() {
        List<ProxyPlayer> players = new ArrayList<>();
        for (ProxiedPlayer player : server.getPlayers())
            players.add(new ProxyPlayer(player.getUniqueId(), player.getName()));
        BungeeProtoHandler.getServers().forEach(server -> server.send(new PlayerList(players)));
    }

    private void onPacket(HandlerCallback.Packet data) {
        if (data.packet() instanceof ProtocolRegister register) {
            registerProtocol(register.namespace(), register.key(), register.packet(), CommonPacketHandler.class, null);
        }
    }
}
