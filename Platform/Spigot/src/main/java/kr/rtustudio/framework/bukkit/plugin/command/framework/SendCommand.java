package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.core.provider.Providers;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.player.PlayerChat;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;

import java.util.List;
import java.util.UUID;

import org.bukkit.permissions.PermissionDefault;

public class SendCommand extends RSCommand<RSFramework> {

    private final Providers providers;

    public SendCommand(RSFramework plugin) {
        super(plugin, "send", PermissionDefault.OP);
        this.providers = plugin.getFramework().getProviders();
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (data.length() > 2) {
            String name = data.args(1);
            String message = data.toString(2);
            UUID uniqueId = providers.getName().getUniqueId(name);
            if (uniqueId == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            if (message.isEmpty()) {
                chat().announce(message().get(player(), "command.empty"));
                return Result.FAILURE;
            }
            ProxyPlayer target = PlayerList.getPlayer(uniqueId);
            if (target == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            PlayerChat.send(
                    target, getPlugin().getPrefix().append(ComponentFormatter.mini(message)));
            return Result.SUCCESS;
        } else if (data.length(2)) {
            chat().announce(message().get(player(), "command.empty"));
            return Result.FAILURE;
        } else return Result.NOT_FOUND_OFFLINE_PLAYER;
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return providers.getName().names(NameProvider.Scope.GLOBAL);
        return List.of();
    }
}
