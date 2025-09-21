package kr.rtustudio.framework.bukkit.core.listener;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import net.Indyuce.mmoitems.api.event.MMOItemsReloadEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class MMOItemsLoaded extends RSListener<RSPlugin> {

    public MMOItemsLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(MMOItemsReloadEvent e) {
        Bukkit.getPluginManager()
                .callEvent(new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.MMOItems));
    }
}
