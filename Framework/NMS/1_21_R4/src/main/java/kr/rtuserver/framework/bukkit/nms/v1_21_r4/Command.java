package kr.rtuserver.framework.bukkit.nms.v1_21_r4;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.nms.NMSCommand;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.CraftCommandMap;

@Getter
public class Command implements NMSCommand {

    private final SimpleCommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();

    @Override
    public Map<String, org.bukkit.command.Command> getKnownCommands() {
        if (MinecraftVersion.isPaper()) return commandMap.getKnownCommands();
        else return ((CraftCommandMap) commandMap).getKnownCommands();
    }

    @Override
    public boolean register(RSCommand<? extends RSPlugin> command) {
        return commandMap.register(command.getName(), command);
    }

    @Override
    public boolean unregister(RSCommand<? extends RSPlugin> command) {
        boolean success = false;
        for (Map.Entry<String, org.bukkit.command.Command> entry :
                Set.copyOf(getKnownCommands().entrySet())) {
            if (entry.getValue() instanceof RSCommand<? extends RSPlugin> rsc) {
                if (command.getPlugin().equals(rsc.getPlugin())) {
                    if (rsc.unregister(commandMap)) {
                        getKnownCommands().remove(entry.getKey());
                        success = true;
                    }
                }
            }
        }
        return success;
    }
}
