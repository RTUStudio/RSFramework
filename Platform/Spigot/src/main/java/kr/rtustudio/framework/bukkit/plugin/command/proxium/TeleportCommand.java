package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import org.bukkit.permissions.PermissionDefault;

/**
 * 크로스 서버 텔레포트 부모 명령어.
 *
 * <p>서브 커맨드:
 *
 * <ul>
 *   <li>{@code /proxium teleport player <플레이어> <대상플레이어>}
 *   <li>{@code /proxium teleport location <플레이어> <서버이름> <월드> <x> <y> <z>}
 * </ul>
 */
public class TeleportCommand extends RSCommand<RSFramework> {

    public TeleportCommand(RSFramework plugin) {
        super(plugin, "teleport", PermissionDefault.OP);
        registerCommand(new TeleportPlayerCommand(plugin));
        registerCommand(new TeleportLocationCommand(plugin));
    }
}
