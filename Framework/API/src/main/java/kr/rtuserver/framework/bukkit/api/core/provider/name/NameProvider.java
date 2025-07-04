package kr.rtuserver.framework.bukkit.api.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.Provider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface NameProvider extends Provider {

    @NotNull
    List<String> getNames();

    @NotNull
    List<String> getNames(boolean includeProxy);

    @NotNull
    String getName(Player player);

    @Nullable
    Player getPlayer(String name);

    @Nullable
    UUID getUniqueId(String name);

}
