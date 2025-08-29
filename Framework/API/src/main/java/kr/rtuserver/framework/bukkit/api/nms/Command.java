package kr.rtuserver.framework.bukkit.api.nms;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;

import java.util.Map;

import org.bukkit.command.SimpleCommandMap;

public interface Command {

    SimpleCommandMap getCommandMap();

    Map<String, org.bukkit.command.Command> getKnownCommands();

    boolean register(RSCommand<? extends RSPlugin> command);

    boolean unregister(RSCommand<? extends RSPlugin> command);
}
