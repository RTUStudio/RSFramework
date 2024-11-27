package kr.rtuserver.framework.bukkit.api.core.config;

import org.bukkit.command.CommandSender;

public interface CommonTranslation {

    String getCommand(String key);

    String getCommand(CommandSender sender, String key);

    String getMessage(String key);

    String getMessage(CommandSender sender, String key);

}
