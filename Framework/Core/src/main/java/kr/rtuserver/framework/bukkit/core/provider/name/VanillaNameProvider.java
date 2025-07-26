package kr.rtuserver.framework.bukkit.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtuserver.framework.bukkit.api.player.PlayerList;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class VanillaNameProvider implements NameProvider {

    @NotNull
    @Override
    public List<String> names(Scope scope) {
        if (scope == Scope.GLOBAL_SERVERS) {
            return PlayerList.getPlayers(true).stream().map(ProxyPlayer::getName).toList();
        } else return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Nullable
    public String getName(UUID uniqueId) {
        for (ProxyPlayer player : PlayerList.getPlayers()) {
            if (player.getUniqueId().equals(uniqueId)) return player.getName();
        }
        return null;
    }

    /**
     * 프록시 플레이어를 지원합니다
     */
    @Nullable
    @Override
    public UUID getUniqueId(String name) {
        for (ProxyPlayer player : PlayerList.getPlayers()) {
            if (player.getName().equals(name)) return player.getUniqueId();
        }
        return null;
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

}
