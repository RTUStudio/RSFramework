package kr.rtuserver.framework.bukkit.api.dependencies;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class RSPlaceholder extends PlaceholderExpansion {

    private final RSPlugin plugin;

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
