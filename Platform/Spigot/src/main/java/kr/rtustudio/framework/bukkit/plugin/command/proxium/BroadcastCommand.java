package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.protocol.internal.BroadcastMessage;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
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

        Proxium proxium = getFramework().getBridge(Proxium.class);
        proxium.publish(BridgeChannel.AUDIENCE, new BroadcastMessage(data.toString(1)));

        notifier.announce(message.get(player(), "command.success"));
        return Result.SUCCESS;
    }
}
