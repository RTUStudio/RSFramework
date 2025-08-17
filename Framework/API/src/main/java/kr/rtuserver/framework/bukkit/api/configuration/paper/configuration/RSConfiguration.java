package kr.rtuserver.framework.bukkit.api.configuration.paper.configuration;

import com.google.common.io.ByteStreams;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@RequiredArgsConstructor
public class RSConfiguration<T extends RSPlugin> {

    @Getter
    private final T plugin;

    private final Map<Class<? extends ConfigurationPart>, ConfigurationImpl<? extends ConfigurationPart>> manager = new HashMap<>();
    private final Map<Class<? extends ConfigurationPart>, ConfigurationPart> config = new HashMap<>();

    public void register(Class<? extends ConfigurationPart> configuration, String name) {
        register(configuration, "Configs", name, null);
    }

    public void register(Class<? extends ConfigurationPart> configuration, String name, Integer version) {
        register(configuration, "Configs", name, version);
    }

    public <C extends ConfigurationPart> void register(Class<C> configuration, String folder, String name, Integer version) {
        name = name.endsWith(".yml") ? name : name + ".yml";
        Path configFolder = plugin.getDataFolder().toPath().resolve(folder);
        Path configFile = configFolder.resolve(name);
        BufferedReader defaultConfig = null;
        try {
            InputStream in = plugin.getResource(folder + "/" + name);
            if (in != null) defaultConfig = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ConfigurationImpl<C> configurationImpl;
        try {
            configurationImpl = new ConfigurationImpl<>(configuration, configFile, defaultConfig, version);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        C instance = configurationImpl.load();
        manager.put(configuration, configurationImpl);
        config.put(configuration, instance);
    }

    public <C extends ConfigurationPart> C get(Class<C> configuration) {
        ConfigurationPart instance = config.get(configuration);
        if (instance == null) return null;
        return (C) instance;
    }

    public void reload() {
        for (Class<? extends ConfigurationPart> configuration : config.keySet()) reload(configuration);
    }

    public <C extends ConfigurationPart> boolean reload(Class<C> configuration) {
        C instance = get(configuration);
        if (instance == null) return false;
        ConfigurationImpl<? extends ConfigurationPart> impl = manager.get(configuration);
        if (impl == null) return false;
        try {
            ((ConfigurationImpl<C>) impl).reload(instance);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}