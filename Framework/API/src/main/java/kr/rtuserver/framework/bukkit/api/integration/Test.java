package kr.rtuserver.framework.bukkit.api.integration;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

public class Test extends PacketAdapter {

    public Test(Plugin plugin) {
        super(new AdapterParameteters()
                .plugin(plugin)
                .listenerPriority(ListenerPriority.HIGHEST)
                .types(PacketType.Play.Server.SPAWN_ENTITY)
                .optionAsync());
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        //debug
    }

}
