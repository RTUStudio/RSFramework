package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import java.util.List;
import java.util.UUID;

import org.bukkit.permissions.PermissionDefault;

/**
 * 크로스 서버 플레이어 대상 텔레포트 명령어.
 *
 * <p>사용법: {@code /proxium teleport player <플레이어> <대상플레이어>}
 */
public class TeleportPlayerCommand extends RSCommand<RSFramework> {

    public TeleportPlayerCommand(RSFramework plugin) {
        super(plugin, "player", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        if (data.length() < 4) return Result.WRONG_USAGE;

        String playerName = data.get(2);
        String targetName = data.get(3);

        UUID playerUuid = provider().getUniqueId(playerName);
        if (playerUuid == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

        UUID targetUuid = provider().getUniqueId(targetName);
        if (targetUuid == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

        ProxyPlayer player = PlayerList.getPlayer(playerUuid);
        if (player == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

        ProxyPlayer target = PlayerList.getPlayer(targetUuid);
        if (target == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

        boolean sent = player.teleport(target);
        if (sent) {
            notifier.announce(message.get(player(), "command.success"));
            return Result.SUCCESS;
        } else {
            notifier.announce(message.get(player(), "teleport.failed"));
            return Result.FAILURE;
        }
    }

    @Override
    public List<String> tabComplete(CommandArgs data) {
        if (data.length(3) || data.length(4)) {
            return provider().names(NameProvider.Scope.GLOBAL);
        }
        return List.of();
    }
}
