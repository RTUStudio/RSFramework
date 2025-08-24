package kr.rtuserver.framework.bukkit.api.configuration;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.internal.SettingConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.internal.StorageConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationType;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtuserver.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

@Slf4j
public class RSConfiguration {

    private final RSPlugin plugin;

    private final Map<Class<? extends ConfigurationPart>, PluginConfiguration<? extends ConfigurationPart>> configuration = new HashMap<>();
    private final Map<Class<? extends ConfigurationPart>, ConfigurationPart> instance = new HashMap<>();

    @Getter
    private final SettingConfiguration setting;
    @Getter
    private final StorageConfiguration storage;

    @Getter
    private MessageTranslation message;
    @Getter
    private CommandTranslation command;

    public RSConfiguration(RSPlugin plugin) {
        this.plugin = plugin;
        this.setting = new SettingConfiguration(plugin);
        this.storage = new StorageConfiguration(plugin);
        this.message = new MessageTranslation(plugin, TranslationType.MESSAGE, this.setting.getLocale());
        this.command = new CommandTranslation(plugin, TranslationType.COMMAND, this.setting.getLocale());
    }

    public void register(Class<? extends ConfigurationPart> configuration, String name) {
        register(configuration, "Configs", name, null);
    }

    public void register(Class<? extends ConfigurationPart> configuration, String name, Integer version) {
        register(configuration, "Configs", name, version);
    }

    public void register(Class<? extends ConfigurationPart> configuration, String folder, String name) {
        register(configuration, folder, name, null);
    }

    public <C extends ConfigurationPart> void register(Class<C> configuration, String folder, String name, Integer version) {
        name = name.endsWith(".yml") ? name : name + ".yml";
        Path configFolder = plugin.getDataFolder().toPath().resolve(folder);
        Path configFile = configFolder.resolve(name);
        BufferedReader defaultConfig = null;
        try {
            InputStream in = plugin.getResource(folder + "/" + name);
            if (in != null) defaultConfig = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
        PluginConfiguration<C> pluginConfiguration;
        try {
            pluginConfiguration = new PluginConfiguration<>(plugin, configuration, configFile, defaultConfig, version);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        C instance = pluginConfiguration.load();
        this.configuration.put(configuration, pluginConfiguration);
        this.instance.put(configuration, instance);
    }

    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> C get(Class<C> configuration) {
        ConfigurationPart instance = this.instance.get(configuration);
        if (instance == null) return null;
        return (C) instance;
    }

    public void reloadInternal() {
        final String locale = setting.getLocale();
        setting.reload();
        storage.reload();
        if (locale.equalsIgnoreCase(setting.getLocale())) {
            message.reload();
            command.reload();
        } else {
            message = new MessageTranslation(plugin, TranslationType.MESSAGE, locale);
            command = new CommandTranslation(plugin, TranslationType.COMMAND, locale);
        }
    }

    public void reloadAll() {
        for (Class<? extends ConfigurationPart> configuration : instance.keySet()) reload(configuration);
    }

    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> boolean reload(Class<C> configuration) {
        C instance = get(configuration);
        if (instance == null) return false;
        PluginConfiguration<? extends ConfigurationPart> impl = this.configuration.get(configuration);
        if (impl == null) return false;
        try {
            ((PluginConfiguration<C>) impl).reload(instance);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Slf4j(topic = "RSConfiguration.Wrapper")
    @SuppressWarnings("unused")
    public static class Wrapper<T extends RSPlugin> {

        @Getter
        private final T plugin;
        private final Path path;
        private final YamlConfigurationLoader loader;
        @Getter
        private final int version;
        @Getter
        private CommentedConfigurationNode config;
        @Getter
        private boolean changed;
        private Wrapper<T> instance;

        public Wrapper(T plugin, String name) {
            this(plugin, "Configs", name, null);
        }

        public Wrapper(T plugin, String name, Integer version) {
            this(plugin, "Configs", name, version);
        }

        public Wrapper(T plugin, String folder, String name) {
            this(plugin, folder, name, null);
        }

        public Wrapper(T plugin, String folder, String name, Integer version) {
            this.plugin = plugin;
            String id = plugin.getName();
            String author = String.join(" & ", plugin.getDescription().getAuthors());
            String header = String.format(PluginConfiguration.HEADER, id, author, id, author);
            this.path = plugin.getDataFolder().toPath().resolve(folder).resolve(name.endsWith(".yml") ? name : name + ".yml");
            YamlConfigurationLoader.Builder builder = YamlConfigurationLoader.builder()
                    .path(path).indent(2)
                    .nodeStyle(NodeStyle.BLOCK)
                    .headerMode(HeaderMode.PRESERVE)
                    .defaultOptions(co -> co.header(header).shouldCopyDefaults(true));
            final BufferedReader defaultConfig = configFromResource(folder, name);
            if (defaultConfig == null) this.loader = builder.build();
            else this.loader = builder.source(() -> defaultConfig).build();
            this.version = version != null ? version : 0;
            try {
                if (Files.notExists(path)) {
                    if (defaultConfig == null) {
                        this.config = CommentedConfigurationNode.root(this.loader.defaultOptions());
                    } else this.config = this.loader.load();
                    if (this.version > 0) this.config.node(Configuration.VERSION_FIELD).raw(version);
                } else this.config = this.loader.load();
            } catch (Exception e) {
                log.warn("Could not initialize {}", folder + "/" + name, e);
                throw new RuntimeException(e);
            }
        }

        private BufferedReader configFromResource(String folder, String name) {
            BufferedReader result = null;
            try {
                InputStream in = plugin.getResource(folder + "/" + name);
                if (in != null) result = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
            return result;
        }

        protected Map<Object, Object> toMap(@NotNull ConfigurationNode node) {
            ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                ConfigurationNode value = entry.getValue();
                if (value == null) continue;
                builder.put(entry.getKey(), value.isMap() ? toMap(value) : value);
            }
            return builder.build();
        }

        public void setup(Wrapper<T> instance) {
            this.instance = instance;
            loadMethod();
            save();
        }

        public void reload() {
            loadConfig();
            loadMethod();
        }

        private void loadConfig() {
            changed = false;
            try {
                final String previous = config.copy().getString();
                config = loader.load();
                if (previous == null) return;
                String dump = config.copy().getString();
                if (!previous.isEmpty()) if (!previous.equalsIgnoreCase(dump)) changed = true;
            } catch (IOException ignore) {
            } catch (Exception ex) {
                log.warn("Could not load {}, please correct your syntax errors", path.getFileName(), ex);
            }
        }

        private void loadMethod() {
            for (Method method : getClass().getDeclaredMethods()) {
                if (Modifier.isPrivate(method.getModifiers())) {
                    if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                        try {
                            method.setAccessible(true);
                            method.invoke(instance);
                        } catch (InvocationTargetException ex) {
                            Throwables.throwIfUnchecked(ex.getCause());
                        } catch (Exception ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "Error invoking " + method, ex);
                        }
                    }
                }
            }
        }

        public void save() {
            if (plugin.isEnabled()) CraftScheduler.async(plugin, scheduledTask -> saveFile());
            else saveFile();
        }

        private void saveFile() {
            try {
                loader.save(config);
            } catch (IOException ex) {
                log.warn("Could not save {}", path.getFileName(), ex);
            }
        }

        protected void set(String path, Object val) {
            set(path, val, "");
        }

        protected String getString(String path, String def) {
            return getString(path, def, new String[]{});
        }

        protected boolean getBoolean(String path, boolean def) {
            return getBoolean(path, def, new String[]{});
        }

        protected double getDouble(String path, double def) {
            return getDouble(path, def, new String[]{});
        }

        protected int getInt(String path, int def) {
            return getInt(path, def, new String[]{});
        }

        protected long getLong(String path, long def) {
            return getLong(path, def, new String[]{});
        }

        protected <E> List<E> getList(String path, Class<E> type, List<E> def) {
            return getList(path, type, def, new String[]{});
        }

        protected List<String> getStringList(String path, List<String> def) {
            return getStringList(path, def, new String[]{});
        }

        protected List<Boolean> getBooleanList(String path, List<Boolean> def) {
            return getBooleanList(path, def, new String[]{});
        }

        protected List<Float> getFloatList(String path, List<Float> def) {
            return getFloatList(path, def, new String[]{});
        }

        protected List<Double> getDoubleList(String path, List<Double> def) {
            return getDoubleList(path, def, new String[]{});
        }

        protected List<Integer> getIntegerList(String path, List<Integer> def) {
            return getIntegerList(path, def, new String[]{});
        }

        protected List<Long> getLongList(String path, List<Long> def) {
            return getLongList(path, def, new String[]{});
        }

        protected Map<Object, Object> getMap(String path, Map<Object, Object> def) {
            return getMap(path, def, new String[]{});
        }

        protected CommentedConfigurationNode set(String path, Object val, String... comment) {
            CommentedConfigurationNode node = pathToNode(path);
            try {
                node.set(val);
                comment(node, comment);
            } catch (SerializationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not set " + path, ex);
            }
            return node;
        }

        protected CommentedConfigurationNode addDefault(String path, Object val, String... comment) {
            CommentedConfigurationNode node = pathToNode(path);
            if (node.virtual()) {
                try {
                    node.set(val);
                } catch (SerializationException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Could not set " + path, ex);
                }
            }
            comment(node, comment);
            return node;
        }

        protected String getString(String path, String def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return node.getString(def);
        }

        protected boolean getBoolean(String path, boolean def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return node.getBoolean(def);
        }

        protected double getDouble(String path, double def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return node.getDouble(def);
        }

        protected int getInt(String path, int def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return node.getInt(def);
        }

        protected long getLong(String path, long def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return node.getLong(def);
        }

        @NotNull
        protected <E> List<E> getList(String path, Class<E> type, List<E> def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            try {
                return node.getList(type, def);
            } catch (SerializationException ex) {
                Throwables.throwIfUnchecked(ex);
                return new ArrayList<>();
            }
        }

        protected List<String> getStringList(String path, List<String> def, String... comment) {
            return getList(path, String.class, def, comment);
        }

        protected List<Boolean> getBooleanList(String path, List<Boolean> def, String... comment) {
            return getList(path, Boolean.class, def, comment);
        }

        protected List<Float> getFloatList(String path, List<Float> def, String... comment) {
            return getList(path, Float.class, def, comment);
        }

        protected List<Double> getDoubleList(String path, List<Double> def, String... comment) {
            return getList(path, Double.class, def, comment);
        }

        protected List<Integer> getIntegerList(String path, List<Integer> def, String... comment) {
            return getList(path, Integer.class, def, comment);
        }

        protected List<Long> getLongList(String path, List<Long> def, String... comment) {
            return getList(path, Long.class, def, comment);
        }

        protected Map<Object, Object> getMap(String path, Map<Object, Object> def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return toMap(node);
        }

        protected Set<String> keys(String path) {
            return keys(pathToNode(path));
        }

        protected Set<String> keys(String path, String... comment) {
            return keys(pathToNode(path));
        }

        protected Set<String> keys() {
            return keys(config);
        }

        protected Set<String> keys(ConfigurationNode node) {
            Set<String> out = new HashSet<>();
            collectKeys(node, "", out);
            return out;
        }

        private void collectKeys(ConfigurationNode node, String prefix, Set<String> out) {
            if (node == null || node.virtual()) return;
            Map<Object, ? extends ConfigurationNode> children = node.childrenMap();
            if (children == null || children.isEmpty()) {
                if (!prefix.isEmpty()) out.add(prefix);
                return;
            }
            for (Map.Entry<Object, ? extends ConfigurationNode> e : children.entrySet()) {
                String next = prefix.isEmpty() ? String.valueOf(e.getKey()) : prefix + "." + e.getKey();
                collectKeys(e.getValue(), next, out);
            }
        }


        private void comment(CommentedConfigurationNode node, String... comment) {
            if (comment.length == 0) return;
            node.comment(String.join("\n", comment));
            try {
                loader.save(config);
            } catch (ConfigurateException ex) {
                throw new RuntimeException(ex);
            }
        }

        protected CommentedConfigurationNode pathToNode(String path) {
            String[] split = path.split("\\.");
            Object[] nodes = new Object[split.length];
            for (int i = 0; i < split.length; i++) {
                try {
                    nodes[i] = Long.parseLong(split[i]);
                } catch (NumberFormatException ex) {
                    nodes[i] = split[i];
                }
            }
            return config.node(nodes);
        }

        private Long parseLong(String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

    }
}