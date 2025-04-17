package kr.rtuserver.framework.bukkit.api.core.player;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface NameProvider {

    @Nullable
    String getName(UUID uuid);

    @Nullable
    UUID getUUID(String name);

}
