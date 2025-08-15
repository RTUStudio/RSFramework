package kr.rtuserver.framework.bukkit.plugin.command.framework;

import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtuserver.framework.bukkit.api.platform.SystemEnvironment;
import kr.rtuserver.framework.bukkit.api.scheduler.CraftScheduler;
import kr.rtuserver.framework.bukkit.plugin.RSFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class InfoCommand extends RSCommand<RSFramework> {

    public InfoCommand(RSFramework plugin) {
        super(plugin, "information", PermissionDefault.OP);
    }

    @Override
    public boolean execute(RSCommandData data) {
        chat().announce(ComponentFormatter.mini(
                "Info\n<gradient:#2979FF:#7C4DFF> ┠ Name<white>: %s</white>\n ┠ Version<white>: %s</white>\n ┠ Bukkit<white>: %s</white>\n ┠ NMS<white>: %s</white>\n ┠ OS<white>: %s</white>\n ┖ JDK<white>: %s</white></gradient>"
                        .formatted(getPlugin().getName()
                                , getPlugin().getDescription().getVersion()
                                , Bukkit.getName() + "-" + MinecraftVersion.getAsText()
                                , framework().getNMSVersion()
                                , SystemEnvironment.getOS()
                                , SystemEnvironment.getJDKVersion())));
        final Player player = player();
        player.sendMessage("테스트 시작");
        CraftScheduler.sync(getPlugin(), s -> {
            player.sendMessage(now() + "1초 뒤에 메세지");
        }).delay(s -> {
            player.sendMessage(now() + "2초 뒤에 메세지");
        }, 20L, false).delay(s -> {
            player.sendMessage(now() + "3초 뒤에 메세지");
        }, 40L, false).delay(s -> {
            player.sendMessage(now() + "반복 시작!");
        }, 60L, false).repeat(s -> {
            player.sendMessage(now() + "1초 쉬고 1초마다 반복");
        }, 20L, 20L, false);
        return true;
    }

    private String now() {
        long currentTimeMillis = System.currentTimeMillis();
        double seconds = currentTimeMillis / 1000.0;
        return "[" + String.format("%.2f", seconds) + "] ";
    }

}
