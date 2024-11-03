package kr.rtuserver.lib.bukkit.plugin.commands;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.lib.bukkit.api.RSPlugin;
import kr.rtuserver.lib.bukkit.api.command.RSCommand;
import kr.rtuserver.lib.bukkit.api.command.RSCommandData;
import kr.rtuserver.lib.bukkit.api.core.RSFramework;
import kr.rtuserver.lib.bukkit.plugin.RSLib;

import java.util.List;

public class RSLibCommand extends RSCommand {

    private final RSLib lib = RSLib.getInstance();
    private final RSFramework framework = LightDI.getBean(RSFramework.class);

    public RSLibCommand(RSPlugin plugin) {
        super(plugin, "rslib", true);

    }

    @Override
    public boolean execute(RSCommandData data) {
        return false;
    }

    @Override
    public void reload(RSCommandData data) {
        getFramework().getModules().reload();
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        return List.of();
    }
}
