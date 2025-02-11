package kr.rtuserver.framework.bukkit.core.listeners;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.events.CustomRegistryLoadedEvent;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class ItemsAdderLoaded extends RSListener<RSPlugin> {

    public ItemsAdderLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(ItemsAdderLoadDataEvent e) {
        Bukkit.getPluginManager().callEvent(new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.ItemsAdder));
    }

}
