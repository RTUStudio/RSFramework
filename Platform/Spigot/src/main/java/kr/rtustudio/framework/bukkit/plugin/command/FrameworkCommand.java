package kr.rtustudio.framework.bukkit.plugin.command;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;
import kr.rtustudio.framework.bukkit.plugin.command.framework.BroadcastCommand;
import kr.rtustudio.framework.bukkit.plugin.command.framework.InfoCommand;
import kr.rtustudio.framework.bukkit.plugin.command.framework.ItemCommand;
import kr.rtustudio.framework.bukkit.plugin.command.framework.SendCommand;

public class FrameworkCommand extends RSCommand<RSFramework> {

    public FrameworkCommand(RSFramework plugin) {
        super(plugin, "rsf");
        registerCommand(new BroadcastCommand(plugin));
        registerCommand(new InfoCommand(plugin));
        registerCommand(new SendCommand(plugin));
        registerCommand(new ItemCommand(plugin));
    }

    @Override
    public void reload(RSCommandData data) {
        framework().getModules().reload();
    }
}
