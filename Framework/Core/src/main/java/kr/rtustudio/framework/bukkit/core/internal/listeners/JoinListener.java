package kr.rtustudio.framework.bukkit.core.internal.listeners;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.core.Framework;
import net.kyori.adventure.audience.Audience;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener extends RSListener<RSPlugin> {

    private final Framework framework;

    public JoinListener(Framework framework, RSPlugin plugin) {
        super(plugin);
        this.framework = framework;
    }

    @EventHandler
    public void motdMessage(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Map<String, RSPlugin> plugins = framework.getPlugins();
        Audience audience = getPlugin().getAdventure().player(player);
        for (RSPlugin plugin : plugins.values()) {
            if (!plugin.getConfiguration().getSetting().isWelcome()) continue;
            String name = gradient(plugin.getName());
            String author = String.join(" & ", plugin.getDescription().getAuthors());
            String str = "%s developed by %s".formatted(name, author);
            audience.sendMessage(ComponentFormatter.mini(str));
        }
    }

    private String gradient(String name) {
        String start = framework.getModules().getTheme().getGradientStart();
        String end = framework.getModules().getTheme().getGradientEnd();
        return "<gradient:" + start + ":" + end + ">" + name + "</gradient>";
    }
}
