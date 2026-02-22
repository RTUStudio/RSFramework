package kr.rtustudio.framework.bukkit.core.provider.name;

import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;

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
        }
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Nullable
    @Override
    public String getName(UUID uniqueId) {
        ProxyPlayer player = PlayerList.getPlayer(uniqueId);
        return player != null ? player.name() : null;
    }

    @Nullable
    @Override
    public UUID getUniqueId(String name) {
        return PlayerList.getPlayers().stream()
                .filter(p -> p.name().equalsIgnoreCase(name))
                .map(ProxyPlayer::uniqueId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }
}
