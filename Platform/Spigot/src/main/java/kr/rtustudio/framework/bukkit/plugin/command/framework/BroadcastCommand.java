package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import org.bukkit.permissions.PermissionDefault;

public class BroadcastCommand extends RSCommand<RSFramework> {

    public BroadcastCommand(RSFramework plugin) {
        super(plugin, "broadcast", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        if (data.get(1).isEmpty()) {
            notifier.announce(message.get(player(), "command.empty"));
            return Result.FAILURE;
        }
        Notifier.broadcastAll(
                getPlugin().getPrefix().append(ComponentFormatter.mini(data.toString(1))));
        return Result.SUCCESS;
    }
}
