package kr.rtustudio.framework.bukkit.core.internal.listeners;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.core.module.ThemeModule;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import net.kyori.adventure.audience.Audience;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings("unused")
public class JoinListener extends RSListener<RSPlugin> {
    public JoinListener(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void motdMessage(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Map<String, RSPlugin> plugins = framework.getPlugins();
        Audience audience = plugin.getAdventure().player(player);
        for (RSPlugin plugin : plugins.values()) {
            if (!plugin.getConfiguration().getSetting().isWelcome()) continue;
            String name = gradient(plugin.getName());
            String author = String.join(" & ", plugin.getDescription().getAuthors());
            String str = "%s developed by %s".formatted(name, author);
            audience.sendMessage(ComponentFormatter.mini(str));
        }
    }

    private String gradient(String name) {
        ThemeModule theme = framework.getModule(ThemeModule.class);
        String start = theme.getGradientStart();
        String end = theme.getGradientEnd();
        return "<gradient:" + start + ":" + end + ">" + name + "</gradient>";
    }
}
