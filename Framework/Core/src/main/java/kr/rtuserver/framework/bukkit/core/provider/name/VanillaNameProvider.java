package kr.rtuserver.framework.bukkit.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.name.NameProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class VanillaNameProvider implements NameProvider {

    @Override
    public List<String> getNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Override
    public String getName(Player player) {
        return player.getName();
    }

    @Override
    public Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

}
