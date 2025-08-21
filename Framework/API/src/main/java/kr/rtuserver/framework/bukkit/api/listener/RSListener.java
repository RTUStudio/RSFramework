package kr.rtuserver.framework.bukkit.api.listener;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.player.PlayerChat;
import lombok.Getter;
import org.bukkit.event.Listener;


@Getter
public abstract class RSListener<T extends RSPlugin> implements Listener {

    @Getter
    private final T plugin;

    private final MessageTranslation message;
    private final CommandTranslation command;
    private final Framework framework = LightDI.getBean(Framework.class);
    private final PlayerChat chat;

    public RSListener(T plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.chat = PlayerChat.of(plugin);
    }

    protected TranslationConfiguration message() {
        return message;
    }

    protected TranslationConfiguration command() {
        return command;
    }

    protected Framework framework() {
        return framework;
    }

    protected PlayerChat chat() {
        return chat;
    }

}
