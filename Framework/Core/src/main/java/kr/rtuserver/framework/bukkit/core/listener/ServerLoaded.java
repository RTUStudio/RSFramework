package kr.rtuserver.framework.bukkit.core.listener;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerLoaded extends RSListener<RSPlugin> {

    public ServerLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(ServerLoadEvent e) {
        Bukkit.getPluginManager()
                .callEvent(new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.Vanilla));
    }
}
