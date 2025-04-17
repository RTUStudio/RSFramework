package kr.rtuserver.framework.bukkit.core.dependency;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.Getter;

@Getter
public class Dependencies {

    private DiscordSRV discordSRV;

    public Dependencies(Framework framework) {
        RSPlugin plugin = framework.getPlugin();
        if (framework.isEnabledDependency("DiscordSRV")) discordSRV = new DiscordSRV(plugin);
    }

}
