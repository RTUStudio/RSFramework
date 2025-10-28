package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.framework.bukkit.api.platform.SystemEnvironment;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;

public class InfoCommand extends RSCommand<RSFramework> {

    public InfoCommand(RSFramework plugin) {
        super(plugin, "information", PermissionDefault.OP);
    }

    @Override
    protected Result execute(RSCommandData data) {
        chat().announce(
                        ComponentFormatter.mini(
                                "Info\n<gradient:#2979FF:#7C4DFF> ┠ Name<white>: %s</white>\n ┠ Version<white>: %s</white>\n ┠ Bukkit<white>: %s</white>\n ┠ NMS<white>: %s</white>\n ┠ OS<white>: %s</white>\n ┖ JDK<white>: %s</white></gradient>"
                                        .formatted(
                                                getPlugin().getName(),
                                                getPlugin().getDescription().getVersion(),
                                                Bukkit.getName()
                                                        + "-"
                                                        + MinecraftVersion.getAsText(),
                                                framework().getNMSVersion(),
                                                SystemEnvironment.getOS(),
                                                SystemEnvironment.getJDKVersion())));
        return Result.SUCCESS;
    }
}
