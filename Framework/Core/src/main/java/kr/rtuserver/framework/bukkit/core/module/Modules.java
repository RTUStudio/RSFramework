package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.core.Framework;
import lombok.Getter;

@Getter
public class Modules implements kr.rtuserver.framework.bukkit.api.core.module.Modules {

    private final Framework framework;
    private final CommandModule command;
    private final ThemeModule theme;

    public Modules(Framework framework) {
        this.framework = framework;
        command = new CommandModule(framework.getPlugin());
        theme = new ThemeModule(framework.getPlugin());
    }

    public void reload() {
        command.reload();
        theme.reload();
    }

}
