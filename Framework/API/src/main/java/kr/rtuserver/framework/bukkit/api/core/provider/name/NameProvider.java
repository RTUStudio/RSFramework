package kr.rtuserver.framework.bukkit.api.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.Provider;
import org.bukkit.entity.Player;

import java.util.List;

public interface NameProvider extends Provider {

    List<String> getNames();

    String getName(Player player);

    Player getPlayer(String name);

}
