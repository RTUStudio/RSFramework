package kr.rtuserver.framework.bukkit.api.core.configuration;

import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationType;
import org.bukkit.command.CommandSender;

public interface CommonTranslation {

    String get(TranslationType type, String key);

    String get(TranslationType type, CommandSender sender, String key);

}
