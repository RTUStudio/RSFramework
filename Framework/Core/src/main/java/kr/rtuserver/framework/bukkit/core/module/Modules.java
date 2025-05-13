package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.Getter;

@Getter
public class Modules implements kr.rtuserver.framework.bukkit.api.core.module.Modules {

    private final Framework framework;
    private final CommandModule commandModule;
    private final ThemeModule themeModule;

    public Modules(Framework framework) {
        this.framework = framework;
        commandModule = new CommandModule(framework.getPlugin());
        themeModule = new ThemeModule(framework.getPlugin());
    }

    public void reload() {
        commandModule.reload();
        themeModule.reload();
    }
}
