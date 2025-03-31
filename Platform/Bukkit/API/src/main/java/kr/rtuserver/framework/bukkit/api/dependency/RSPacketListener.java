package kr.rtuserver.framework.bukkit.api.dependency;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.impl.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import lombok.Getter;

public abstract class RSPacketListener<T extends RSPlugin> extends PacketAdapter {

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

    public RSPacketListener(T plugin, AdapterParameteters parameters) {
        super(parameters.plugin(plugin));
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.chat = PlayerChat.of(plugin);
    }

    public boolean register() {
        if (plugin.getFramework().isEnabledDependency("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().addPacketListener(this);
            return true;
        } else return false;
    }

    public boolean unregister() {
        if (plugin.getFramework().isEnabledDependency("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().removePacketListener(this);
            return true;
        } else return false;
    }

}
