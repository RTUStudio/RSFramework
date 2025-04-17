package kr.rtuserver.framework.bukkit.core.command;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.configuration.impl.Translation;
import kr.rtuserver.framework.bukkit.api.nms.NMSCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends RSCommand<RSPlugin> {

    public ReloadCommand(RSPlugin plugin) {
        super(plugin, "reload", PermissionDefault.OP);
    }

    @Override
    protected boolean execute(RSCommandData data) {
        getPlugin().getConfigurations().reload();
        NMSCommand nmsc = framework().getNMS().getCommand();
        for (RSCommand<? extends RSPlugin> rsc : getPlugin().getCommands()) {
            nmsc.unregister(rsc);
            List<String> aliases = new ArrayList<>(getNames().subList(1, getNames().size()));
            for (Translation translation : command().getMap().values()) {
                String name = translation.get(getName() + ".name");
                if (getName().equals(name)) continue;
                aliases.add(name);
            }
            super.setAliases(aliases);
            nmsc.register(rsc);
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        }
        chat().announce(message().getCommon(player(), "reload"));
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
