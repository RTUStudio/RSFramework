package kr.rtuserver.framework.bukkit.core.listeners;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.events.PluginItemsLoadedEvent;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;

public class VanillaServerLoaded extends RSListener<RSPlugin> {

    public VanillaServerLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(ServerLoadEvent e) {
        Bukkit.getPluginManager().callEvent(new PluginItemsLoadedEvent(PluginItemsLoadedEvent.Plugin.VANILLA));
    }

}