package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtuserver.framework.bukkit.core.Framework;
import lombok.Getter;

@Getter
public class Modules implements kr.rtuserver.framework.bukkit.api.core.module.Modules {

    private final Framework framework;
    private final RSConfiguration configuration;

    public Modules(Framework framework) {
        this.framework = framework;
        this.configuration = framework.getPlugin().getConfiguration();

        this.configuration.register(CommandModule.class, "Modules", "Command");
        this.configuration.register(ThemeModule.class, "Modules", "Theme");
    }

    public CommandModule getCommand() {
        return this.configuration.get(CommandModule.class);
    }

    public ThemeModule getTheme() {
        return this.configuration.get(ThemeModule.class);
    }

    public void reload() {
        this.configuration.reload(CommandModule.class);
        this.configuration.reload(ThemeModule.class);
    }
}
