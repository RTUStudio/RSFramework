package kr.rtuserver.lib.bungee;

import kr.rtuserver.protoweaver.api.impl.bungee.BungeeProtoWeaver;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.plugin.Plugin;

@Slf4j(topic = "RSLib")
public class RSLib extends Plugin {

    private BungeeProtoWeaver protoWeaver;

    @Override
    public void onEnable() {
        log.info("RSLib Bungee loaded.");
        protoWeaver = new kr.rtuserver.protoweaver.core.impl.bungee.BungeeProtoWeaver(getProxy(), getDataFolder().toPath());
        getProxy().getPluginManager().registerListener(this, protoWeaver);
    }

    @Override
    public void onDisable() {
        protoWeaver.disable();
    }
}
