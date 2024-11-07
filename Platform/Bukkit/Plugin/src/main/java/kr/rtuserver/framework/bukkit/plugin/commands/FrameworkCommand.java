package kr.rtuserver.framework.bukkit.plugin.commands;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.config.impl.CommandConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.MessageConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import kr.rtuserver.framework.bukkit.plugin.RSFramework;

import java.util.List;

public class FrameworkCommand extends kr.rtuserver.framework.bukkit.api.command.RSCommand {

    private final RSFramework rsf = RSFramework.getInstance();
    private final MessageConfiguration message = rsf.getConfigurations().getMessage();
    private final CommandConfiguration command = rsf.getConfigurations().getCommand();
    private final Framework framework = LightDI.getBean(Framework.class);

    public FrameworkCommand(RSPlugin plugin) {
        super(plugin, "rsf", true);

    }

    @Override
    public boolean execute(RSCommandData data) {
        PlayerChat chat = PlayerChat.of(getPlugin());
        if (data.equals(0, command.get("command.broadcast"))) {
            if (data.args(1).isEmpty()) {
                chat.announce(getSender(), message.get("command.broadcast.empty"));
            } else chat.broadcastAll(message.get("command.broadcast.prefix") + data.args(1));
        }
        return false;
    }

    @Override
    public void reload(RSCommandData data) {
        getFramework().getModules().reload();
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        return List.of(command.get("command.broadcast"));
    }
}
