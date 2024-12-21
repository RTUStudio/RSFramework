package kr.rtuserver.framework.bukkit.core.listeners;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.events.PluginItemsLoadedEvent;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import net.Indyuce.mmoitems.api.event.MMOItemsReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class MMOItemsLoaded extends RSListener<RSPlugin> {

    public MMOItemsLoaded(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLoad(MMOItemsReloadEvent e) {
        Bukkit.getPluginManager().callEvent(new PluginItemsLoadedEvent(PluginItemsLoadedEvent.Plugin.MMOITEMS));
    }

}