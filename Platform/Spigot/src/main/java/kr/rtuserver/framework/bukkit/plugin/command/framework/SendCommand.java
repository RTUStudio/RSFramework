package kr.rtuserver.framework.bukkit.plugin.command.framework;

import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.configuration.translation.message.MessageTranslation;
import kr.rtuserver.framework.bukkit.api.core.provider.Providers;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.player.PlayerChat;
import kr.rtuserver.framework.bukkit.api.player.PlayerList;
import kr.rtuserver.framework.bukkit.plugin.RSFramework;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.UUID;

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
                chat().announce(message().getCommon(player(), MessageTranslation.NOT_FOUND_OFFLINE_PLAYER));
                return true;
            }
            if (message.isEmpty()) {
                chat().announce(message().get(player(), "command.empty"));
                return true;
            }
            ProxyPlayer target = PlayerList.getPlayer(uniqueId);
            if (target == null) {
                chat().announce(message().getCommon(player(), MessageTranslation.NOT_FOUND_OFFLINE_PLAYER));
                return true;
            }
            PlayerChat.send(target, getPlugin().getPrefix().append(ComponentFormatter.mini(message)));
        } else if (data.length(2)) chat().announce(message().get(player(), "command.empty"));
        else chat().announce(message().getCommon(player(), MessageTranslation.NOT_FOUND_OFFLINE_PLAYER));
        return true;
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return providers.getName().names();
        return List.of();
    }

}
