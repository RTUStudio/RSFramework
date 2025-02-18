package kr.rtuserver.framework.bukkit.api.config;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.utility.platform.FileResource;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

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
    @Getter
    private final YamlFile config;
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
        this.config = new YamlFile(file);
        load();
        if (version != null) set("version", version);
        this.version = version != null ? version : 1;
        String id = plugin.getName();
        String author = String.join(" & ", plugin.getDescription().getAuthors());
        config.options().header(String.format(HEADER, id, author, id, author));
        config.options().copyDefaults(true);
    }

    protected static Map<String, Object> toMap(ConfigurationSection section) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object obj = section.get(key);
                if (obj != null) {
                    builder.put(key, obj instanceof ConfigurationSection val ? toMap(val) : obj);
                }
            }
        }
        return builder.build();
    }

    public void setup(RSConfiguration<T> instance) {
        this.instance = instance;
        init();
        save();
    }

    protected void log(Level level, String s) {
        Bukkit.getLogger().log(level, s);
    }

    public void reload() {
        load();
        init();
    }

    public void load() {
        changed = false;
        try {
            final String previous = config.dump();
            config.loadWithComments();
            String dump = config.dump();
            if (!previous.isEmpty()) if (!previous.equalsIgnoreCase(dump)) changed = true;
        } catch (IOException ignore) {
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("Could not load %s, please correct your syntax errors", this.file.getName()), ex);
            Throwables.throwIfUnchecked(ex);
        }
    }

    private void init() {
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
        if (plugin.isEnabled()) Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveFile);
        else saveFile();
    }

    private void saveFile() {
        try {
            config.save(file);
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

    protected <E> List<E> getList(String path, List<E> def) {
        return getList(path, def, new String[]{});
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

    protected Map<String, Object> getMap(String path, Map<String, Object> def) {
        return getMap(path, def, new String[]{});
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return getConfigurationSection(path, "");
    }

    protected void set(String path, Object val, String... comment) {
        config.addDefault(path, val);
        if (comment.length != 0) setComment(path, comment);
        config.set(path, val);
    }

    protected String getString(String path, String def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        return config.getString(path, config.getString(path));
    }

    protected boolean getBoolean(String path, boolean def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        return config.getBoolean(path, config.getBoolean(path));
    }

    protected double getDouble(String path, double def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        return config.getDouble(path, config.getDouble(path));
    }

    protected int getInt(String path, int def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        return config.getInt(path, config.getInt(path));
    }

    protected long getLong(String path, long def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        return config.getLong(path, config.getLong(path));
    }

    protected <E> List<E> getList(String path, List<E> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        return (List<E>) config.getList(path, config.getList(path));
    }

    protected List<String> getStringList(String path, List<String> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        List<String> result = config.getStringList(path);
        return result != null ? result : def;
    }

    protected List<Boolean> getBooleanList(String path, List<Boolean> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        List<Boolean> result = config.getBooleanList(path);
        return result != null ? result : def;
    }

    protected List<Float> getFloatList(String path, List<Float> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        List<Float> result = config.getFloatList(path);
        return result != null ? result : def;
    }

    protected List<Double> getDoubleList(String path, List<Double> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        List<Double> result = config.getDoubleList(path);
        return result != null ? result : def;
    }

    protected List<Integer> getIntegerList(String path, List<Integer> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        List<Integer> result = config.getIntegerList(path);
        return result != null ? result : def;
    }

    protected List<Long> getLongList(String path, List<Long> def, String... comment) {
        config.addDefault(path, def);
        if (comment.length != 0) setComment(path, comment);
        List<Long> result = config.getLongList(path);
        return result != null ? result : def;
    }

    protected Map<String, Object> getMap(String path, Map<String, Object> def, String... comment) {
        if (def != null && config.getConfigurationSection(path) == null) {
            config.addDefault(path, def);
            if (comment.length != 0) setComment(path, comment);
            return def;
        }
        return toMap(config.getConfigurationSection(path));
    }

    public ConfigurationSection getDefaultSection() {
        return config.getDefaultSection();
    }

    public ConfigurationSection getConfigurationSection(String path, String... comment) {
        if (comment.length != 0) setComment(path, comment);
        return config.getConfigurationSection(path);
    }

    private void setComment(String path, String... comment) {
        config.setComment(path, String.join("\n", comment), CommentType.BLOCK);
    }
}
