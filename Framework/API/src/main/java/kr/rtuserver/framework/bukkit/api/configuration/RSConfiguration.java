package kr.rtuserver.framework.bukkit.api.configuration;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.platform.FileResource;
import kr.rtuserver.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class RSConfiguration<T extends RSPlugin> {

    private final String HEADER = """
            ╔ Developed by ════════════════════════════════════╗
            ║ ░█▀▄░░▀█▀░░█░█░░░░░█▀▀░░▀█▀░░█░█░░█▀▄░░▀█▀░░█▀█░ ║
            ║ ░█▀▄░░░█░░░█░█░░░░░▀▀█░░░█░░░█░█░░█░█░░░█░░░█░█░ ║
            ║ ░▀░▀░░░▀░░░▀▀▀░░░░░▀▀▀░░░▀░░░▀▀▀░░▀▀░░░▀▀▀░░▀▀▀░ ║
            ╚══════════════════════════════════════════════════╝
            
            This is the configuration for %s.
            If you have any questions or need assistance,
            please join our Discord server and ask for help from %s!
            
            이것은 %s의 구성입니다.
            질문이 있거나 도움이 필요하시면,
            저희 Discord 서버에 가입하셔서 %s에게 도움을 요청해 주세요!""";

    @Getter
    private final T plugin;
    private final File file;
    private final YamlConfigurationLoader loader;
    @Getter
    private CommentedConfigurationNode config;
    @Getter
    private final int version;
    @Getter
    private boolean changed;
    private RSConfiguration<T> instance;

    public RSConfiguration(T plugin, String name, Integer version) {
        this(plugin, "Configs", name, version);
    }

    public RSConfiguration(T plugin, String folder, String name, Integer version) {
        this.plugin = plugin;
        this.file = FileResource.createFileCopy(plugin, folder, name);
        if (file == null) throw new IllegalArgumentException("Could not find file " + folder + "/" + name);
        this.loader = YamlConfigurationLoader.builder().file(file).build();
        try {
            config = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        loadConfig();
        if (version != null) set("version", version);
        this.version = version != null ? version : 1;
        String id = plugin.getName();
        String author = String.join(" & ", plugin.getDescription().getAuthors());
        config.options().header(String.format(HEADER, id, author, id, author));
        config.options().shouldCopyDefaults(true);
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

    public void setup(RSConfiguration<T> instance) {
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
            String dump = config.copy().getString();
            if (!previous.isEmpty()) if (!previous.equalsIgnoreCase(dump)) changed = true;
        } catch (IOException ignore) {
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("Could not load %s, please correct your syntax errors", this.file.getName()), ex);
            Throwables.throwIfUnchecked(ex);
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
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + file, ex);
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

    protected String getString(String path, String def, String... comment) {
        CommentedConfigurationNode node = set(path, def, comment);
        return node.getString(def);
    }

    protected boolean getBoolean(String path, boolean def, String... comment) {
        CommentedConfigurationNode node = set(path, def, comment);
        return node.getBoolean(def);
    }

    protected double getDouble(String path, double def, String... comment) {
        CommentedConfigurationNode node = set(path, def, comment);
        return node.getDouble(def);
    }

    protected int getInt(String path, int def, String... comment) {
        CommentedConfigurationNode node = set(path, def, comment);
        return node.getInt(def);
    }

    protected long getLong(String path, long def, String... comment) {
        CommentedConfigurationNode node = set(path, def, comment);
        return node.getLong(def);
    }

    @NotNull
    protected <E> List<E> getList(String path, Class<E> type, List<E> def, String... comment) {
        CommentedConfigurationNode node = set(path, def, comment);
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
        CommentedConfigurationNode node = set(path, def, comment);
        return toMap(node);
    }

    protected Set<String> keys() {
        Set<Object> keys = config.childrenMap().keySet();
        return keys.stream().map(Object::toString).collect(Collectors.toSet());
    }

    protected Set<String> keys(String path) {
        return keys(path, new String[]{});
    }

    protected Set<String> keys(String path, String... comment) {
        CommentedConfigurationNode node = pathToNode(path);
        Set<Object> keys = node.childrenMap().keySet();
        return keys.stream().map(Object::toString).collect(Collectors.toSet());

    }

    private void comment(CommentedConfigurationNode node, String... comment) {
        if (comment.length == 0) return;
        node.comment(String.join("\n", comment));
    }

    protected CommentedConfigurationNode pathToNode(String path) {
        String[] split = path.split("\\.");
        Object[] nodes = new Object[split.length];
        for (int i = 0; i < split.length; i++) {
            try {
                nodes[i] = Long.parseLong(split[i]);
            } catch (NumberFormatException e) {
                nodes[i] = split[i];
            }
        }
        return config.node(nodes);
    }

    private Long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
