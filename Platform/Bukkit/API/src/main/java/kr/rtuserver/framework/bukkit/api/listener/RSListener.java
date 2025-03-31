package kr.rtuserver.framework.bukkit.api.listener;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.impl.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import lombok.Getter;
import org.bukkit.event.Listener;


@Getter
public abstract class RSListener<T extends RSPlugin> implements Listener {

    @Getter
    private final T plugin;

    private final TranslationConfiguration message;

    protected TranslationConfiguration message() {
        return message;
    }

    private final TranslationConfiguration command;

    protected TranslationConfiguration command() {
        return command;
    }

    private final Framework framework = LightDI.getBean(Framework.class);

    protected Framework framework() {
        return framework;
    }

    private final PlayerChat chat;

    protected PlayerChat chat() {
        return chat;
    }

    public RSListener(T plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.chat = PlayerChat.of(plugin);
    }
}
