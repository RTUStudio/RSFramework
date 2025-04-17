package kr.rtuserver.framework.bukkit.api.dependency;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.impl.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.player.PlayerChat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class RSPlaceholder<T extends RSPlugin> extends PlaceholderExpansion {

    private final T plugin;

    private final TranslationConfiguration message;
    private final TranslationConfiguration command;
    private final Framework framework = LightDI.getBean(Framework.class);
    private final PlayerChat chat;

    public RSPlaceholder(T plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
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
        if (offlinePlayer != null && offlinePlayer.isOnline()) chat.setReceiver(offlinePlayer.getPlayer());
        return request(offlinePlayer, params.split("_"));
    }

    public abstract String request(OfflinePlayer offlinePlayer, String[] params);
}
