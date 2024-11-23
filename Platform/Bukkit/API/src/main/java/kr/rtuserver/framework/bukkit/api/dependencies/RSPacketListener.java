package kr.rtuserver.framework.bukkit.api.dependencies;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import lombok.Getter;

@Getter
public abstract class RSPacketListener extends PacketAdapter {

    private final RSPlugin plugin;

    public RSPacketListener(RSPlugin plugin, AdapterParameteters parameteters) {
        super(parameteters.plugin(plugin));
        this.plugin = plugin;
    }

    public boolean register() {
        if (plugin.getFramework().isEnabledDependency("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().addPacketListener(this);
            return true;
        } else return false;
    }

    public boolean unregister() {
        if (plugin.getFramework().isEnabledDependency("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().removePacketListener(this);
            return true;
        } else return false;
    }

}
