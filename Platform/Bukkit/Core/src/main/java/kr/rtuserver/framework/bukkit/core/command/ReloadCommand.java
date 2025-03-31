package kr.rtuserver.framework.bukkit.core.command;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class ReloadCommand extends RSCommand<RSPlugin> {

    public ReloadCommand(RSPlugin plugin) {
        super(plugin, "reload", PermissionDefault.OP);
    }

    @Override
    protected boolean execute(RSCommandData data) {
        getPlugin().getConfigurations().reload();
        chat().announce(audience(), message().getCommon(player(), "reload"));
        return true;
    }

    @Override
    protected String getLocalizedDescription(Player player) {
        return command().getCommon(player, getDescription());
    }

    @Override
    protected String getLocalizedUsage(Player player) {
        return command().getCommon(player, getUsage());
    }

    @Override
    protected String getLocalizedName(Player player) {
        return command().getCommon(player, getName() + ".name");
    }

}
