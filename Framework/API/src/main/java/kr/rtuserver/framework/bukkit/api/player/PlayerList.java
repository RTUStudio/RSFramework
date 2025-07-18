package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.bukkit.api.BukkitProtoWeaver;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class PlayerList {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static ProxyPlayer getPlayer(UUID uniqueId) {
        return getPlayers().stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public static Set<ProxyPlayer> getPlayers() {
        return getPlayers(true);
    }

    public static Set<ProxyPlayer> getPlayers(boolean includeProxy) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (protoWeaver.isConnected() && includeProxy) return new HashSet<>(protoWeaver.getPlayers().values());
        return Bukkit.getOnlinePlayers().stream().map(player -> getPlayer(player.getUniqueId())).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}