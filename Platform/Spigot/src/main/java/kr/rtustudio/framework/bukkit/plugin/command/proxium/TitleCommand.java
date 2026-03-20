package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.bukkit.permissions.PermissionDefault;

public class TitleCommand extends RSCommand<RSFramework> {

    public TitleCommand(RSFramework plugin) {
        super(plugin, "title", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        if (data.length() > 2) {
            String name = data.get(1);
            String titleStr = data.get(2);
            String subtitleStr = data.length() > 3 ? data.toString(3) : "";

            UUID uniqueId = provider().getUniqueId(name);
            if (uniqueId == null) return Result.NOT_FOUND_OFFLINE_PLAYER;
            if (titleStr.isEmpty()) {
                notifier.announce(message.get(player(), "command.empty"));
                return Result.FAILURE;
            }
            ProxyPlayer target = PlayerList.getPlayer(uniqueId);
            if (target == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

            Title title =
                    Title.title(
                            ComponentFormatter.mini(titleStr),
                            ComponentFormatter.mini(subtitleStr),
                            Title.Times.times(
                                    Duration.ofMillis(500),
                                    Duration.ofMillis(3000),
                                    Duration.ofMillis(500)));

            target.showTitle(title);
            notifier.announce(message.get(player(), "command.success"));
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
