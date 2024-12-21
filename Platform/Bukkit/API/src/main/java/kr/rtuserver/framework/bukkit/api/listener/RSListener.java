package kr.rtuserver.framework.bukkit.api.listener;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.impl.SettingConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.core.config.CommonTranslation;
import lombok.Getter;
import org.bukkit.event.Listener;


@Getter
public abstract class RSListener<T extends RSPlugin> implements Listener {

    private final T plugin;
    private final SettingConfiguration setting;
    private final TranslationConfiguration message;
    private final TranslationConfiguration command;
    private final CommonTranslation common;

    public RSListener(T plugin) {
        this.plugin = plugin;
        this.setting = plugin.getConfigurations().getSetting();
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.common = plugin.getFramework().getCommonTranslation();
    }
}
