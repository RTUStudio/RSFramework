package kr.rtuserver.framework.bukkit.api.dependency;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.impl.SettingConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.impl.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class RSPlaceholder<T extends RSPlugin> extends PlaceholderExpansion {

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

    public RSPlaceholder(T plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.chat = PlayerChat.of(plugin);
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        return request(offlinePlayer, params.split("_"));
    }

    public abstract String request(OfflinePlayer offlinePlayer, String[] params);
}
