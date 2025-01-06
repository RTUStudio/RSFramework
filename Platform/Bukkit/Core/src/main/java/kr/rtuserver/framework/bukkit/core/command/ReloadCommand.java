package kr.rtuserver.framework.bukkit.core.command;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends RSCommand<RSPlugin> {

    public ReloadCommand(RSPlugin plugin) {
        super(plugin, "reload", PermissionDefault.OP);
    }

    @Override
    protected boolean execute(RSCommandData data) {
        getPlugin().getConfigurations().reload();
        getChat().announce(getSender(), getCommon().getMessage(getPlayer(), "reload"));
        return true;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "common.command." + getName() + ".description";
    }

    @NotNull
    @Override
    public String getUsage() {
        return "common.command." + getName() + ".usage";
    }

    @Override
    protected String getLocalizedName(Player player) {
        return getCommon().getCommand(player, getName());
    }

}
