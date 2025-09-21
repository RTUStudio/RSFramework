package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
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
    public boolean execute(RSCommandData data) {
        if (data.length() > 2) {
            String name = data.args(1);
            String message = data.toString(2);
            UUID uniqueId = providers.getName().getUniqueId(name);
            if (uniqueId == null) {
                chat().announce(
                                message()
                                        .getCommon(
                                                player(),
                                                MessageTranslation.NOT_FOUND_OFFLINE_PLAYER));
                return true;
            }
            if (message.isEmpty()) {
                chat().announce(message().get(player(), "command.empty"));
                return true;
            }
            ProxyPlayer target = PlayerList.getPlayer(uniqueId);
            if (target == null) {
                chat().announce(
                                message()
                                        .getCommon(
                                                player(),
                                                MessageTranslation.NOT_FOUND_OFFLINE_PLAYER));
                return true;
            }
            PlayerChat.send(
                    target, getPlugin().getPrefix().append(ComponentFormatter.mini(message)));
        } else if (data.length(2)) chat().announce(message().get(player(), "command.empty"));
        else
            chat().announce(
                            message()
                                    .getCommon(
                                            player(), MessageTranslation.NOT_FOUND_OFFLINE_PLAYER));
        return true;
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return providers.getName().names(NameProvider.Scope.GLOBAL_SERVERS);
        return List.of();
    }
}
