package kr.rtuserver.framework.bukkit.plugin.command.framework;

import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.plugin.RSFramework;
import org.bukkit.permissions.PermissionDefault;

public class BroadcastCommand extends RSCommand<RSFramework> {

    public BroadcastCommand(RSFramework plugin) {
        super(plugin, "broadcast", PermissionDefault.OP);
    }

    @Override
    public boolean execute(RSCommandData data) {
        if (data.args(1).isEmpty()) {
            chat().announce(message().get(player(), "command.broadcast.empty"));
        } else chat().broadcastAll(getPlugin().getPrefix().append(ComponentFormatter.mini(data.args(1))));
        return true;
    }

}
