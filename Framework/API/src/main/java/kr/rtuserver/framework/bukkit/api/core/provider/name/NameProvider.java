package kr.rtuserver.framework.bukkit.api.core.provider.name;

import kr.rtuserver.framework.bukkit.api.core.provider.Provider;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NameProvider extends Provider {

    @NotNull
    List<String> names(Scope scope);

    /**
     * Get all player names on current server
     *
     * @return player names
     */
    default @NotNull List<String> names() {
        return names(Scope.CURRENT_SERVER);
    }

    @Nullable
    String getName(UUID uniqueId);

    @Nullable
    UUID getUniqueId(String name);

    enum Scope {
        GLOBAL_SERVERS,
        CURRENT_SERVER
    }
}
