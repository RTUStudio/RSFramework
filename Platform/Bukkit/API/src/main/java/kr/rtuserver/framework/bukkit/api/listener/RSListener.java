package kr.rtuserver.framework.bukkit.api.listener;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.impl.CommandConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.MessageConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.SettingConfiguration;
import lombok.Getter;
import org.bukkit.event.Listener;


@Getter
public abstract class RSListener implements Listener {

    private final RSPlugin plugin;
    private final SettingConfiguration setting;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    public RSListener(RSPlugin plugin) {
        this.plugin = plugin;
        this.setting = plugin.getConfigurations().getSetting();
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
    }
}
