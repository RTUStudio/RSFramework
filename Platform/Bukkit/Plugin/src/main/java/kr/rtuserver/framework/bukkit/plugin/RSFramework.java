package kr.rtuserver.framework.bukkit.plugin;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.plugin.commands.FrameworkCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class RSFramework extends RSPlugin {

    @Getter
    private static RSFramework instance;
    @Getter
    private final Libraries libraries;

    public RSFramework() {
        libraries = new Libraries(this);
        List<String> list = new ArrayList<>();
        list.add("kr.rtuserver.framework.bukkit");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof RSPlugin) {
                list.add(plugin.getClass().getPackageName());
            }
        }
        LightDI.init(list.toArray(new String[0]));
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
        registerPermission(getName() + ".broadcast", PermissionDefault.OP);
        registerPermission(getName() + ".information", PermissionDefault.OP);
        registerCommand(new FrameworkCommand(this));
    }

    @Override
    protected void disable() {
        getFramework().disable(this);
    }
}
