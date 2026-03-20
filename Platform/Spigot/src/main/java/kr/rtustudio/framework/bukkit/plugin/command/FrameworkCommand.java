package kr.rtustudio.framework.bukkit.plugin.command;

import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;
import kr.rtustudio.framework.bukkit.plugin.command.framework.InfoCommand;
import kr.rtustudio.framework.bukkit.plugin.command.framework.ItemCommand;

public class FrameworkCommand extends RSCommand<RSFramework> {

    public FrameworkCommand(RSFramework plugin) {
        super(plugin, "rsf");
        registerCommand(new InfoCommand(plugin));
        registerCommand(new ItemCommand(plugin));
    }

    @Override
    public void reload(CommandArgs data) {
        framework.getModuleFactory().reload();
    }
}
