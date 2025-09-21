package kr.rtustudio.framework.bukkit.core.listener;

import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;

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
