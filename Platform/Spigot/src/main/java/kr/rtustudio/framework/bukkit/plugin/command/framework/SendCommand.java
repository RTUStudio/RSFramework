package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.player.PlayerAudience;
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
    protected Result execute(RSCommandData data) {
        if (data.length() > 2) {
            String name = data.args(1);
            String message = data.toString(2);
            UUID uniqueId = provider().getUniqueId(name);
            if (uniqueId == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            if (message.isEmpty()) {
                chat().announce(message().get(player(), "command.empty"));
                return Result.FAILURE;
            }
            ProxyPlayer target = PlayerList.getPlayer(uniqueId);
            if (target == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            PlayerAudience.send(
                    target, getPlugin().getPrefix().append(ComponentFormatter.mini(message)));
            return Result.SUCCESS;
        } else if (data.length(2)) {
            chat().announce(message().get(player(), "command.empty"));
            return Result.FAILURE;
        } else return Result.NOT_FOUND_OFFLINE_PLAYER;
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return provider().names(NameProvider.Scope.GLOBAL);
        return List.of();
    }
}
