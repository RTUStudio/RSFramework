package kr.rtustudio.framework.bukkit.api.integration.wrapper;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.integration.Integration;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class PlaceholderWrapper<T extends RSPlugin> implements Integration {

    @Getter protected final T plugin;
    @Getter protected final Framework framework;
    @Getter protected final MessageTranslation message;
    @Getter protected final CommandTranslation command;
    @Getter protected final Notifier notifier;
    private final String identifier;
    private Integration.Wrapper wrapper;

    public PlaceholderWrapper(T plugin) {
        this(plugin, plugin.getName().toLowerCase());
    }

    public PlaceholderWrapper(T plugin, String identifier) {
        this.plugin = plugin;
        this.framework = LightDI.getBean(Framework.class);
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.notifier = Notifier.of(plugin);
        this.identifier = identifier;
    }

    @Override
    public boolean isAvailable() {
        return framework.isEnabledDependency("PlaceholderAPI");
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

    public abstract String onRequest(OfflinePlayer offlinePlayer, PlaceholderArgs params);

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
                notifier.setReceiver(offlinePlayer.getPlayer());
            return PlaceholderWrapper.this.onRequest(offlinePlayer, new PlaceholderArgs(params));
        }
    }
}
