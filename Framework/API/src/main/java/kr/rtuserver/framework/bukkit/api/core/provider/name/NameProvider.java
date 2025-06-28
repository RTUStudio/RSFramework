package kr.rtuserver.framework.bukkit.api.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.Provider;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface NameProvider extends Provider {

    List<String> getNames();

    List<String> getNames(boolean includeProxy);

    String getName(Player player);

    Player getPlayer(String name);

    UUID getUniqueId(String name);

}
