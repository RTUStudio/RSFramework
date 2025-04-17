package kr.rtuserver.framework.bukkit.core.player;

import kr.rtuserver.framework.bukkit.api.core.player.NameProvider;
import kr.rtuserver.framework.bukkit.core.Framework;
import kr.rtuserver.framework.bukkit.core.dependency.Dependencies;
import kr.rtuserver.framework.bukkit.core.dependency.DiscordSRV;
import kr.rtuserver.framework.bukkit.core.module.Modules;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NameProviderImpl implements NameProvider {

    private final Dependencies dependencies;
    private final Modules modules;

    public NameProviderImpl(Framework framework) {
        this.dependencies = framework.getDependencies();
        this.modules = framework.getModules();
    }

    @Nullable
    @Override
    public String getName(UUID uuid) {
        switch (modules.getCommandModule().getTabCompletePlayersType()) {
            case DISCORD_SRV -> {
                DiscordSRV discordSRV = dependencies.getDiscordSRV();
                if (discordSRV != null) return discordSRV.getName(uuid);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public UUID getUUID(String name) {
        switch (modules.getCommandModule().getTabCompletePlayersType()) {
            case DISCORD_SRV -> {
                DiscordSRV discordSRV = dependencies.getDiscordSRV();
                if (discordSRV != null) return discordSRV.getUUID(name);
            }
        }
        return null;
    }
}
