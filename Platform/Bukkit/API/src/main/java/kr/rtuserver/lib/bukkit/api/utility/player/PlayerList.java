package kr.rtuserver.lib.bukkit.api.utility.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.lib.bukkit.api.core.RSFramework;
import kr.rtuserver.protoweaver.api.ProxyPlayer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerList {

    static RSFramework framework;

    static RSFramework framework() {
        if (framework == null) framework = LightDI.getBean(RSFramework.class);
        return framework;
    }

    public static List<ProxyPlayer> getPlayers() {
        List<ProxyPlayer> list = framework().getProtoWeaver().getPlayers();
        if (!list.isEmpty()) return list;
        List<ProxyPlayer> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            result.add(new ProxyPlayer(player.getUniqueId(), player.getName()));
        }
        return result;
    }

    public static List<String> getName() {
        return getPlayers().stream().map(ProxyPlayer::name).toList();
    }

    public static List<UUID> getUUID() {
        return getPlayers().stream().map(ProxyPlayer::uuid).toList();
    }
}