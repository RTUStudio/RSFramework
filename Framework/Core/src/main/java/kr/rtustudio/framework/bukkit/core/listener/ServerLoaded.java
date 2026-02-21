package kr.rtustudio.framework.bukkit.core.listener;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.event.CustomRegistryLoadedEvent;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;

@SuppressWarnings("unused")
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
