package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.protoweaver.api.ProxyPlayer;
import kr.rtuserver.protoweaver.api.impl.bukkit.BukkitProtoWeaver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerList {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static Set<ProxyPlayer> getPlayer() {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        boolean isConnected = protoWeaver.isConnected();
        return isConnected ? protoWeaver.getPlayers() : Bukkit.getOnlinePlayers().stream().map(player -> new ProxyPlayer(player.getUniqueId(), player.getName())).collect(Collectors.toSet());
    }

    public static List<String> getName() {
        return getPlayer().stream().map(ProxyPlayer::name).toList();
    }

    public static List<UUID> getUUID() {
        return getPlayer().stream().map(ProxyPlayer::uuid).toList();
    }

}