package kr.rtuserver.lib.bukkit.plugin;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.lib.bukkit.api.RSPlugin;
import kr.rtuserver.lib.bukkit.plugin.commands.RSLibCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;

    public RSLib() {
        super();
        List<String> list = new ArrayList<>();
        list.add(kr.rtuserver.lib.bukkit.core.RSFramework.class.getPackageName());
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof RSPlugin) {
                list.add(plugin.getClass().getPackageName());
            }
        }
        LightDI.init(list.toArray(new String[list.size()]));
    }

    @Override
    protected void initialize() {
        getFramework().load(this);
    }

    @Override
    protected void load() {
        instance = this;
    }

    @Override
    protected void enable() {
        getFramework().enable(this);

        registerPermission(getName() + ".motd", PermissionDefault.OP);
        registerCommand(new RSLibCommand(this));
    }

    @Override
    protected void disable() {
        getFramework().disable(this);
    }
}
