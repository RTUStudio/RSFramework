package kr.rtuserver.framework.bukkit.api.configuration;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.setting.SettingConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.storage.StorageConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationType;
import kr.rtuserver.framework.bukkit.api.configuration.translation.command.CommandTranslation;
import kr.rtuserver.framework.bukkit.api.configuration.translation.message.MessageTranslation;
import lombok.Getter;

@Getter
public class Configurations {

    private final RSPlugin plugin;

    private final SettingConfiguration setting;
    private final StorageConfiguration storage;

    private MessageTranslation message;
    private CommandTranslation command;

    public Configurations(RSPlugin plugin) {
        this.plugin = plugin;
        setting = new SettingConfiguration(plugin);
        storage = new StorageConfiguration(plugin);
        message = new MessageTranslation(plugin, TranslationType.MESSAGE, setting.getLocale());
        command = new CommandTranslation(plugin, TranslationType.COMMAND, setting.getLocale());
    }

    public void reload() {
        final String locale = setting.getLocale();
        setting.reload();
        storage.reload();
        if (locale.equalsIgnoreCase(setting.getLocale())) {
            message.reload();
            command.reload();
        } else {
            message = new MessageTranslation(plugin, TranslationType.MESSAGE, locale);
            command = new CommandTranslation(plugin, TranslationType.COMMAND, locale);
        }
    }

}
