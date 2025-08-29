package kr.rtuserver.framework.bukkit.core.listener;

import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class OraxenLoaded extends RSListener<RSPlugin> {

    public OraxenLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(OraxenItemsLoadedEvent e) {
        Bukkit.getPluginManager()
                .callEvent(new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.Oraxen));
    }
}
