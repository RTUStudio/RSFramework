package kr.rtustudio.framework.bukkit.core.module;

import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.api.core.module.Module;
import kr.rtustudio.framework.bukkit.core.Framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleFactory implements kr.rtustudio.framework.bukkit.api.core.module.ModuleFactory {
    private final RSConfiguration configuration;
    private final Map<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();

    public ModuleFactory(Framework framework) {
        this.configuration = framework.getPlugin().getConfiguration();
        this.configuration.registerConfiguration(
                CommandModule.class, ConfigPath.of("Module", "Command"));
        this.configuration.registerConfiguration(
                ThemeModule.class, ConfigPath.of("Module", "Theme"));
        register(
                kr.rtustudio.framework.bukkit.api.core.module.CommandModule.class,
                this.configuration.get(CommandModule.class));
        register(
                kr.rtustudio.framework.bukkit.api.core.module.ThemeModule.class,
                this.configuration.get(ThemeModule.class));
    }

    private <T extends Module> void register(Class<T> type, T module) {
        modules.put(type, module);
    }

    @Override
    public <T extends Module> T getModule(Class<T> type) {
        return type.cast(modules.get(type));
    }

    @Override
    public <T extends Module> void setModule(T module) {
        modules.put(module.getClass(), module);
    }

    @Override
    public void reload() {
        this.configuration.reload(CommandModule.class);
        this.configuration.reload(ThemeModule.class);
    }
}
