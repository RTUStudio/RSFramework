package kr.rtustudio.framework.bukkit.api.integration.wrapper;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.integration.Integration;
import kr.rtustudio.framework.bukkit.api.player.PlayerAudience;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class PlaceholderWrapper<T extends RSPlugin> implements Integration {

    private final T plugin;

    private final MessageTranslation message;
    private final CommandTranslation command;
    private final Framework framework = LightDI.getBean(Framework.class);
    private final PlayerAudience chat;

    private Integration.Wrapper wrapper;

    private final String identifier;

    public PlaceholderWrapper(T plugin) {
        this(plugin, plugin.getName().toLowerCase());
    }

    public PlaceholderWrapper(T plugin, String identifier) {
        this.plugin = plugin;
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.chat = PlayerAudience.of(plugin);
        this.identifier = identifier;
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

    protected PlayerAudience chat() {
        return chat;
    }

    @Override
    public boolean isAvailable() {
        return plugin.getFramework().isEnabledDependency("PlaceholderAPI");
    }

    @Override
    public boolean register() {
        wrapper = new PlaceholderAPI(identifier);
        return wrapper.register();
    }

    @Override
    public boolean unregister() {
        boolean result = wrapper.unregister();
        if (result) wrapper = null;
        return result;
    }

    public abstract String onRequest(OfflinePlayer offlinePlayer, String[] params);

    @RequiredArgsConstructor
    class PlaceholderAPI extends PlaceholderExpansion implements Integration.Wrapper {

        private final String identifier;

        @Override
        public @NotNull String getAuthor() {
            return plugin.getDescription().getAuthors().getFirst();
        }

        @Override
        public @NotNull String getIdentifier() {
            return identifier;
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
            if (offlinePlayer != null && offlinePlayer.isOnline())
                chat.setReceiver(offlinePlayer.getPlayer());
            return PlaceholderWrapper.this.onRequest(offlinePlayer, params.split("_"));
        }
    }
}
