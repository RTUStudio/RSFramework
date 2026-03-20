package kr.rtustudio.framework.bukkit.api.nms;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

import java.util.Map;

import org.bukkit.command.SimpleCommandMap;

public interface Command {

    SimpleCommandMap getCommandMap();

    Map<String, org.bukkit.command.Command> getKnownCommands();

    boolean register(RSCommand<? extends RSPlugin> command);

    boolean unregister(RSCommand<? extends RSPlugin> command);
}
