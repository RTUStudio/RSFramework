package kr.rtuserver.framework.bukkit.core.internal.listeners;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.core.Framework;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class JoinListener extends RSListener {

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
            if (!plugin.getConfigurations().getSetting().isWelcome()) continue;
            String name = gradient(plugin.getName());
            String author = String.join(" & ", plugin.getDescription().getAuthors());
            String str = "%s developed by %s".formatted(name, author);
            audience.sendMessage(ComponentFormatter.mini(str));
        }
    }

    private String gradient(String name) {
        String start = framework.getModules().getThemeModule().getGradientStart();
        String end = framework.getModules().getThemeModule().getGradientEnd();
        return "<gradient:" + start + ":" + end + ">" + name + "</gradient>";
    }
}
