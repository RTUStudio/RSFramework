package kr.rtuserver.framework.bukkit.core.listener;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class NexoLoaded extends RSListener<RSPlugin> {

    public NexoLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(NexoItemsLoadedEvent e) {
        Bukkit.getPluginManager().callEvent(new CustomRegistryLoadedEvent(CustomRegistryLoadedEvent.Type.Nexo));
    }

}
