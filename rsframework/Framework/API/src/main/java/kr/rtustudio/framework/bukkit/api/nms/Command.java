package kr.rtustudio.framework.bukkit.api.nms;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

import java.util.Map;

import org.bukkit.command.SimpleCommandMap;

/**
 * NMS interface for server command registration and management.
 *
 * <p>서버 커맨드 등록 및 관리 NMS 인터페이스.
 */
public interface Command {

    SimpleCommandMap getCommandMap();

    Map<String, org.bukkit.command.Command> getKnownCommands();

    boolean register(RSCommand<? extends RSPlugin> command);

    boolean unregister(RSCommand<? extends RSPlugin> command);
}
