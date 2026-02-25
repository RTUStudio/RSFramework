package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import java.util.List;
import java.util.UUID;

import org.bukkit.permissions.PermissionDefault;

public class SendCommand extends RSCommand<RSFramework> {

    public SendCommand(RSFramework plugin) {
        super(plugin, "send", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        if (data.length() > 2) {
            String name = data.get(1);
            String text = data.toString(2);
            UUID uniqueId = provider().getUniqueId(name);
            if (uniqueId == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            if (text.isEmpty()) {
                notifier.announce(message.get(player(), "command.empty"));
                return Result.FAILURE;
            }
            ProxyPlayer target = PlayerList.getPlayer(uniqueId);
            if (target == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            Notifier.send(target, getPlugin().getPrefix().append(ComponentFormatter.mini(text)));
            return Result.SUCCESS;
        } else if (data.length(2)) {
            notifier.announce(message.get(player(), "command.empty"));
            return Result.FAILURE;
        } else return Result.NOT_FOUND_OFFLINE_PLAYER;
    }

    @Override
    public List<String> tabComplete(CommandArgs data) {
        if (data.length(2)) return provider().names(NameProvider.Scope.GLOBAL);
        return List.of();
    }
}
