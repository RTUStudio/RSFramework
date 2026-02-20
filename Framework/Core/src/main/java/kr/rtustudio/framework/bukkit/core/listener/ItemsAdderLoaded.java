package kr.rtustudio.framework.bukkit.core.listener;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

@SuppressWarnings("unused")
public class ItemsAdderLoaded extends RSListener<RSPlugin> {

    public ItemsAdderLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(ItemsAdderLoadDataEvent e) {
        Bukkit.getPluginManager()
                .callEvent(
                        new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.ItemsAdder));
    }
}
