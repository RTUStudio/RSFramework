package kr.rtustudio.framework.bukkit.core.provider.name;

import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaNameProvider implements NameProvider {

    @NotNull
    @Override
    public List<String> names(Scope scope) {
        if (scope == Scope.GLOBAL) {
            return PlayerList.getPlayers(true).stream().map(ProxyPlayer::name).toList();
        } else return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Nullable
    public String getName(UUID uniqueId) {
        for (ProxyPlayer player : PlayerList.getPlayers()) {
            if (player.uniqueId().equals(uniqueId)) return player.name();
        }
        return null;
    }

    /** 프록시 플레이어를 지원합니다 */
    @Nullable
    @Override
    public UUID getUniqueId(String name) {
        for (ProxyPlayer player : PlayerList.getPlayers()) {
            if (player.name().equals(name)) return player.uniqueId();
        }
        return null;
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }
}
