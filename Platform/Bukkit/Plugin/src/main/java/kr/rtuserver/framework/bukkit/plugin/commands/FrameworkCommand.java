package kr.rtuserver.framework.bukkit.plugin.commands;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.config.impl.CommandConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.MessageConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.utility.platform.MinecraftVersion;
import kr.rtuserver.framework.bukkit.api.utility.platform.SystemEnvironment;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import kr.rtuserver.framework.bukkit.plugin.RSFramework;
import org.bukkit.Bukkit;

import java.util.ArrayList;
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
        if (data.equals(0, command.get("broadcast"))) {
            if (hasPermission(getName() + ".broadcast")) {
                if (data.args(1).isEmpty()) {
                    chat.announce(getSender(), message.get("command.broadcast.empty"));
                } else chat.broadcastAll(message.get("command.broadcast.prefix") + data.args(1));
            } else chat.announce(getSender(), message.getCommon("noPermission"));
            return true;
        }
        if (data.equals(0, command.get("information"))) {
            if (hasPermission(getName() + ".information")) {
                chat.announce(getAudience(), ComponentFormatter.mini(
                        "Info\n<gradient:#2979FF:#7C4DFF> ┠ Name<white>: %s</white>\n ┠ Version<white>: %s</white>\n ┠ Bukkit<white>: %s</white>\n ┠ NMS<white>: %s</white>\n ┠ OS<white>: %s</white>\n ┖ JDK<white>: %s</white></gradient>"
                                .formatted(getPlugin().getName()
                                        , getPlugin().getDescription().getVersion()
                                        , Bukkit.getName() + "-" + MinecraftVersion.getAsText()
                                        , getFramework().getNMSVersion()
                                        , SystemEnvironment.getOS()
                                        , SystemEnvironment.getJDKVersion())));
            } else chat.announce(getSender(), message.getCommon("noPermission"));
            return true;
        }
        return false;
    }

    @Override
    public void wrongUsage(RSCommandData data) {
        PlayerChat chat = PlayerChat.of(getPlugin());
        if (hasPermission(getPlugin().getName() + ".broadcast"))
            chat.announce(getSender(), String.format("<gray> - </gray>/%s %s", getName(), command.get("broadcast")));
        if (hasPermission(getPlugin().getName() + ".information"))
            chat.announce(getSender(), String.format("<gray> - </gray>/%s %s", getName(), command.get("information")));
    }

    @Override
    public void reload(RSCommandData data) {
        getFramework().getModules().reload();
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        if (data.length(1)) {
            List<String> list = new ArrayList<>();
            if (hasPermission(getName() + ".broadcast")) list.add(command.get("broadcast"));
            if (hasPermission(getName() + ".information")) list.add(command.get("information"));
            return list;
        }
        return List.of();
    }
}
