package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.player.PlayerList;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.permissions.PermissionDefault;

/**
 * 크로스 서버 좌표 대상 텔레포트 명령어.
 *
 * <p>사용법: {@code /proxium teleport location <플레이어> <서버이름> <월드> <x> <y> <z>}
 */
public class TeleportLocationCommand extends RSCommand<RSFramework> {

    public TeleportLocationCommand(RSFramework plugin) {
        super(plugin, "location", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        // teleport location <player> <server> <world> <x> <y> <z>
        // index:  0        1        2        3       4     5  6  7
        if (data.length() < 8) return Result.WRONG_USAGE;

        String playerName = data.get(2);
        String serverName = data.get(3);
        String worldName = data.get(4);

        UUID playerUuid = provider().getUniqueId(playerName);
        if (playerUuid == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

        ProxyPlayer player = PlayerList.getPlayer(playerUuid);
        if (player == null) return Result.NOT_FOUND_OFFLINE_PLAYER;

        Proxium proxium = getFramework().getBridge(Proxium.class);
        ProxiumNode node = proxium.getNode(serverName);
        if (node == null) {
            notifier.announce(message.get(player(), "teleport.unknown-server"));
            return Result.FAILURE;
        }

        Double x = parseDouble(data.get(5));
        Double y = parseDouble(data.get(6));
        Double z = parseDouble(data.get(7));

        if (x == null || y == null || z == null) {
            notifier.announce(message.get(player(), "teleport.invalid-coordinates"));
            return Result.FAILURE;
        }

        ProxyLocation location = new ProxyLocation(node, worldName, x, y, z);
        boolean sent = player.teleport(location);

        if (sent) {
            notifier.announce(message.get(player(), "command.success"));
            return Result.SUCCESS;
        } else {
            notifier.announce(message.get(player(), "teleport.failed"));
            return Result.FAILURE;
        }
    }

    @Override
    public List<String> tabComplete(CommandArgs data) {
        return switch (data.length()) {
            case 3 -> provider().names(NameProvider.Scope.GLOBAL);
            case 4 -> {
                Proxium proxium = getFramework().getBridge(Proxium.class);
                yield proxium.getPlayers().values().stream()
                        .map(ProxyPlayer::getServer)
                        .filter(s -> s != null)
                        .distinct()
                        .collect(Collectors.toList());
            }
            case 5 ->
                    Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            case 6 -> List.of("~");
            case 7 -> List.of("~");
            case 8 -> List.of("~");
            default -> List.of();
        };
    }

    private static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
