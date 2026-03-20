package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import org.bukkit.permissions.PermissionDefault;

public class ProxiumCommand extends RSCommand<RSFramework> {

    public ProxiumCommand(RSFramework plugin) {
        super(plugin, "proxium", PermissionDefault.OP);
        registerCommand(new ActionbarCommand(plugin));
        registerCommand(new TitleCommand(plugin));
        registerCommand(new BroadcastCommand(plugin));
        registerCommand(new SendCommand(plugin));
        registerCommand(new TestCommand(plugin));
    }
}
