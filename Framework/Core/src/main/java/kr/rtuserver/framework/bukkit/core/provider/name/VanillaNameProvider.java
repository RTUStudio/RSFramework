package kr.rtuserver.framework.bukkit.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtuserver.framework.bukkit.api.player.RSPlayer;
import kr.rtuserver.framework.bukkit.core.Framework;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class VanillaNameProvider implements NameProvider {

    @Override
    public List<String> getNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Override
    public List<String> getNames(boolean includeProxy) {
        if (includeProxy) return RSPlayer.getPlayers(true).stream().map(ProxyPlayer::getName).toList();
        else return getNames();
    }

    @Override
    public String getName(Player player) {
        return player.getName();
    }

    @Override
    public Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    /**
     * 프록시 플레이어를 지원합니다
     */
    @Override
    public UUID getUniqueId(String name) {
        for (ProxyPlayer player : RSPlayer.getPlayers()) {
            if (player.getName().equals(name)) return player.getUniqueId();
        }
        return null;
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

}
