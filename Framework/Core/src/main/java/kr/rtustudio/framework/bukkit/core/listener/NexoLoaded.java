package kr.rtustudio.framework.bukkit.core.listener;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;

public class NexoLoaded extends RSListener<RSPlugin> {

    public NexoLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(NexoItemsLoadedEvent e) {
        Bukkit.getPluginManager()
                .callEvent(new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.Nexo));
    }
}
