package kr.rtustudio.framework.bukkit.api.configuration;

import kr.rtustudio.configurate.model.*;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.SettingConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import com.google.common.base.Throwables;

/**
 * Centrally manages all configuration files for a plugin. Handles internal configs (Setting,
 * Message, Command) and custom {@link ConfigurationPart} registrations. Operates on Configurate
 * YAML loaders.
 *
 * <p>플러그인의 모든 설정 파일을 통합 관리하는 클래스. 내부 설정(Setting, Message, Command)과 커스텀 {@link ConfigurationPart}를
 * 등록·로드·리로드한다.
 */
@Slf4j
public class RSConfiguration {

    private final RSPlugin plugin;

    private final Map<Class<? extends ConfigurationPart>, Registry<? extends ConfigurationPart>>
            registries = new HashMap<>();
    @Getter private final SettingConfiguration setting;
    @Getter private MessageTranslation message;
    @Getter private CommandTranslation command;

    /**
     * Initializes all internal configurations for the plugin.
     *
     * <p>플러그인의 모든 내부 설정을 초기화한다.
     *
     * @param plugin the owning plugin
     */
    public RSConfiguration(RSPlugin plugin) {
        this.plugin = plugin;
        this.setting = new SettingConfiguration(plugin);
        this.message =
                new MessageTranslation(plugin, TranslationType.MESSAGE, this.setting.getLocale());
        this.command =
                new CommandTranslation(plugin, TranslationType.COMMAND, this.setting.getLocale());
    }

    /**
     * Registers a single {@code .yml} configuration file.
     *
     * <p>The last element of {@link ConfigPath} is interpreted as the filename, and the rest as the
     * folder path.
     *
     * <p>단일 {@code .yml} 설정 파일을 등록한다. {@link ConfigPath}의 마지막 요소가 파일명, 나머지가 폴더 경로로 해석된다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path configuration path (e.g. {@code ConfigPath.of("MyConfig")})
     */
    public <C extends ConfigurationPart> C registerConfiguration(
            Class<C> configuration, ConfigPath path) {
        return registerConfiguration(configuration, path, null);
    }

    /**
     * Registers a single {@code .yml} configuration file including custom serialization.
     *
     * <p>커스텀 직렬화를 포함하여 단일 {@code .yml} 설정 파일을 등록한다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path configuration path
     * @param extraSerializer additional type serializers
     */
    public <C extends ConfigurationPart> C registerConfiguration(
            Class<C> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> extraSerializer) {
        C instance =
                registerImpl(
                        configuration,
                        path.folder(),
                        path.version(),
                        path.fileName(),
                        extraSerializer);

        registries.put(
                configuration,
                new Registry<>(path, extraSerializer, false, Map.of(path.last(), instance)));

        return instance;
    }

    /**
     * Registers all {@code .yml} files in a folder as individual instances.
     *
     * <p>All elements of {@link ConfigPath} are interpreted as the folder path.
     *
     * <p>폴더 내 모든 {@code .yml} 파일을 개별 인스턴스로 등록한다. {@link ConfigPath}의 전체 요소가 폴더 경로로 해석된다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path folder path (e.g. {@code ConfigPath.of("Regions")})
     * @return {@link ConfigList} using the filename (excluding extension) as key
     */
    public <C extends ConfigurationPart> ConfigList<C> registerConfigurations(
            Class<C> configuration, ConfigPath path) {
        return registerConfigurations(configuration, path, null);
    }

    /**
     * Registers all {@code .yml} files in a folder including custom serialization.
     *
     * <p>커스텀 직렬화를 포함하여 폴더 내 모든 {@code .yml} 파일을 등록한다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path folder path
     * @param extraSerializer additional type serializers
     * @return {@link ConfigList} using the filename (excluding extension) as key
     */
    public <C extends ConfigurationPart> ConfigList<C> registerConfigurations(
            Class<C> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> extraSerializer) {
        String folderPath = path.folderPath();
        Path configFolder = plugin.getDataFolder().toPath().resolve(folderPath);
        Map<String, C> result = new LinkedHashMap<>();
        try {
            Files.createDirectories(configFolder);
            Set<String> fileNames = new LinkedHashSet<>();
            try (java.util.stream.Stream<Path> stream = Files.list(configFolder)) {
                stream.filter(p -> p.toString().endsWith(".yml"))
                        .forEach(file -> fileNames.add(file.getFileName().toString()));
            }
            fileNames.addAll(getJarResourceNames(folderPath));

            fileNames.stream()
                    .sorted()
                    .forEach(
                            name -> {
                                String key = name.replace(".yml", "");
                                C instance =
                                        registerImpl(
                                                configuration,
                                                folderPath,
                                                path.version(),
                                                name,
                                                extraSerializer);
                                result.put(key, instance);
                            });
        } catch (IOException e) {
            log.warn("Could not scan folder {}", folderPath, e);
        }
        registries.put(configuration, new Registry<>(path, extraSerializer, true, result));
        return new ConfigList<>(result);
    }

    private <C extends ConfigurationPart> C registerImpl(
            Class<C> configuration,
            String folder,
            Integer version,
            String name,
            Consumer<TypeSerializerCollection.Builder> extraSerializer) {
        Path configFile = plugin.getDataFolder().toPath().resolve(folder).resolve(name);
        BufferedReader defaultConfig = configFromResource(folder, name);
        try {
            return new PluginConfiguration<>(
                            plugin,
                            configuration,
                            configFile,
                            defaultConfig,
                            version,
                            extraSerializer)
                    .load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collects a list of {@code .yml} file names belonging to the specified folder path from
     * resources within the JAR.
     *
     * <p>JAR 내부의 리소스에서 지정 폴더 경로에 속하는 {@code .yml} 파일 이름 목록을 수집한다.
     *
     * @param folderPath resource folder path
     * @return set of {@code .yml} file names (direct files only, excluding subfolders)
     */
    private Set<String> getJarResourceNames(String folderPath) {
        Set<String> names = new LinkedHashSet<>();
        try {
            URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            if (url != null) {
                File jarFile = new File(url.toURI());
                if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(jarFile)) {
                        String prefix =
                                folderPath.isEmpty()
                                        ? ""
                                        : folderPath + (folderPath.endsWith("/") ? "" : "/");
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith(prefix)
                                    && name.endsWith(".yml")
                                    && !entry.isDirectory()) {
                                String relative = name.substring(prefix.length());
                                if (!relative.contains("/")) names.add(relative);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return names;
    }

    /**
     * Reads a configuration file from resources within the plugin JAR.
     *
     * <p>플러그인 JAR 내부의 리소스에서 설정 파일을 읽어온다.
     *
     * @param folder resource folder path
     * @param name file name
     * @return {@link BufferedReader} if resource exists, {@code null} otherwise
     */
    @Nullable
    private BufferedReader configFromResource(String folder, String name) {
        try {
            InputStream in = plugin.getResource(folder + "/" + name);
            if (in != null) {
                return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Looks up a registered configuration instance by type.
     *
     * <p>등록된 설정 인스턴스를 타입으로 조회한다.
     *
     * @param configuration {@link ConfigurationPart} class to lookup
     * @param <C> configuration type
     * @return registered configuration instance, or {@code null} if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> C get(Class<C> configuration) {
        Registry<C> registry = (Registry<C>) this.registries.get(configuration);
        if (registry == null || registry.instances().isEmpty()) return null;
        return registry.instances().values().iterator().next();
    }

    /**
     * Looks up a registered configuration list instance by type.
     *
     * <p>등록된 설정 목록 인스턴스를 타입으로 조회한다.
     *
     * @param configuration {@link ConfigurationPart} class to lookup
     * @param <C> configuration type
     * @return registered configuration list instance, or {@code null} if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> ConfigList<C> getList(Class<C> configuration) {
        Registry<C> registry = (Registry<C>) this.registries.get(configuration);
        if (registry == null) return null;
        return new ConfigList<>(registry.instances());
    }

    /**
     * Reloads all internal configurations (Setting, Storage, Message, Command). If the locale has
     * changed, new translation objects are created.
     *
     * <p>내부 설정(Setting, Storage, Message, Command)을 모두 리로드한다. 로케일이 변경된 경우 번역 객체를 새로 생성한다.
     */
    public void reloadInternal() {
        final String locale = setting.getLocale();
        setting.reload();
        plugin.getFramework().reloadStorages(plugin);
        if (locale.equalsIgnoreCase(setting.getLocale())) {
            message.reload();
            command.reload();
        } else {
            message = new MessageTranslation(plugin, TranslationType.MESSAGE, locale);
            command = new CommandTranslation(plugin, TranslationType.COMMAND, locale);
        }
    }

    /**
     * Reloads all registered custom configurations from files.
     *
     * <p>등록된 모든 커스텀 설정을 파일에서 다시 로드한다.
     */
    public void reloadAll() {
        for (Class<? extends ConfigurationPart> configuration : this.registries.keySet())
            reload(configuration);
    }

    /**
     * Reloads the specified configuration from files. This reloads both single configurations and
     * configuration lists registered as folders. For folders, this also reflects newly added or
     * deleted files.
     *
     * <p>지정한 설정을 파일에서 다시 로드한다. 단일 설정뿐만 아니라 폴더로 등록된 설정 목록도 함께 리로드하며, 폴더의 경우 새로 추가/삭제된 파일까지 모두 반영하여
     * 갱신한다.
     *
     * @param configuration {@link ConfigurationPart} class to reload
     * @param <C> configuration type
     * @return whether the reload logic executed successfully
     */
    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> boolean reload(Class<C> configuration) {
        Registry<C> registry = (Registry<C>) this.registries.get(configuration);
        if (registry == null) return false;

        try {
            String folder =
                    registry.isList() ? registry.path().folderPath() : registry.path().folder();
            for (Map.Entry<String, C> entry : registry.instances().entrySet()) {
                String name = entry.getKey() + ".yml";
                Path configFile = plugin.getDataFolder().toPath().resolve(folder).resolve(name);
                BufferedReader defaultConfig = configFromResource(folder, name);

                new PluginConfiguration<>(
                                plugin,
                                configuration,
                                configFile,
                                defaultConfig,
                                registry.path().version(),
                                registry.extraSerializer())
                        .reload(entry.getValue());
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to reload configuration {}", configuration.getSimpleName(), e);
            return false;
        }
    }

    private record Registry<C extends ConfigurationPart>(
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> extraSerializer,
            boolean isList,
            Map<String, C> instances) {}

    /**
     * Wrapper class for managing a single YAML configuration file. Provides node read/write,
     * default value handling, and change detection via Configurate {@link YamlConfigurationLoader}.
     * Subclass {@code private void} methods are automatically invoked during {@link #setup} and
     * {@link #reload}.
     *
     * <p>YAML 설정 파일 하나를 관리하는 래퍼 클래스. Configurate {@link YamlConfigurationLoader} 기반으로 노드 읽기/쓰기, 기본값
     * 처리, 변경 감지 등을 제공한다.
     *
     * @param <T> owning plugin type
     */
    @Slf4j(topic = "RSConfiguration.Wrapper")
    @SuppressWarnings("unused")
    public static class Wrapper<T extends RSPlugin> {

        @Getter protected final T plugin;
        private final Path path;
        private final YamlConfigurationLoader loader;
        @Getter private final int version;
        @Getter private CommentedConfigurationNode config;
        @Getter private boolean changed;
        private Wrapper<T> instance;

        public Wrapper(T plugin, String name) {
            this(plugin, "Config", name, null);
        }

        public Wrapper(T plugin, String name, Integer version) {
            this(plugin, "Config", name, version);
        }

        public Wrapper(T plugin, String folder, String name) {
            this(plugin, folder, name, null);
        }

        public Wrapper(T plugin, ConfigPath path) {
            this(plugin, path.folder(), path.fileName(), path.version());
        }

        public Wrapper(T plugin, String folder, String name, Integer version) {
            this.plugin = plugin;
            String fileName = name.endsWith(".yml") ? name : name + ".yml";
            String id = plugin.getName();
            String author = String.join(" & ", plugin.getDescription().getAuthors());
            String header = String.format(PluginConfiguration.HEADER, id, author, id, author);
            this.path = plugin.getDataFolder().toPath().resolve(folder).resolve(fileName);
            YamlConfigurationLoader.Builder builder =
                    YamlConfigurationLoader.builder()
                            .path(path)
                            .indent(2)
                            .nodeStyle(NodeStyle.BLOCK)
                            .headerMode(HeaderMode.PRESERVE)
                            .defaultOptions(co -> ConfigurationSerializer.apply(co.header(header)));
            final BufferedReader defaultConfig = configFromResource(folder, fileName);
            if (defaultConfig == null || Files.exists(this.path)) this.loader = builder.build();
            else this.loader = builder.source(() -> defaultConfig).build();
            this.version = version != null ? version : 0;
            try {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                if (Files.notExists(path)) {
                    this.config =
                            defaultConfig != null
                                    ? this.loader.load()
                                    : CommentedConfigurationNode.root(this.loader.defaultOptions());
                    if (this.version > 0)
                        this.config.node(Configuration.VERSION_FIELD).raw(version);
                } else {
                    try {
                        this.config = this.loader.load();
                    } catch (ParsingException e) {
                        this.config = CommentedConfigurationNode.root(this.loader.defaultOptions());
                        if (!(e.getCause() instanceof NullPointerException)) {
                            LoggerFactory.getLogger(plugin.getName())
                                    .error("An error occurred {}", e.getCause().getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not initialize {}", folder + "/" + fileName, e);
                throw new RuntimeException(e);
            }
        }

        @Nullable
        private BufferedReader configFromResource(String folder, String name) {
            try {
                InputStream in = plugin.getResource(folder + "/" + name);
                if (in != null)
                    return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
            return null;
        }

        @NotNull
        protected Map<Object, Object> toMap(@NotNull ConfigurationNode node) {
            Map<Object, Object> map = new LinkedHashMap<>();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry :
                    node.childrenMap().entrySet()) {
                ConfigurationNode value = entry.getValue();
                if (value == null) continue;
                map.put(entry.getKey(), value.isMap() ? toMap(value) : value);
            }
            return Collections.unmodifiableMap(map);
        }

        public void setup(Wrapper<T> instance) {
            this.instance = instance;
            loadMethod();
            saveFile();
        }

        public void reload() {
            loadConfig();
            loadMethod();
            save();
        }

        private void loadConfig() {
            changed = false;
            try {
                final String previous = config.copy().getString();
                config = loader.load();
                if (previous != null && !previous.isEmpty()) {
                    changed = !previous.equalsIgnoreCase(config.copy().getString());
                }
            } catch (IOException ex) {
                log.warn("IOException {}", path.getFileName(), ex);
            } catch (Exception ex) {
                log.warn(
                        "Could not load {}, please correct your syntax errors",
                        path.getFileName(),
                        ex);
            }
        }

        private void loadMethod() {
            for (Method method : getClass().getDeclaredMethods()) {
                if (!Modifier.isPrivate(method.getModifiers())) continue;
                if (method.getParameterCount() != 0 || method.getReturnType() != Void.TYPE)
                    continue;
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (InvocationTargetException ex) {
                    Throwables.throwIfUnchecked(ex.getCause());
                } catch (Exception ex) {
                    log.error("Error invoking {}", method, ex);
                }
            }
        }

        public void save() {
            if (plugin.isEnabled()) CraftScheduler.async(plugin, this::saveFile);
            else saveFile();
        }

        private void saveFile() {
            String lock = path.toAbsolutePath().toString().intern();
            synchronized (lock) {
                try {
                    if (path.getParent() != null && Files.notExists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    loader.save(config);
                } catch (IOException ex) {
                    log.warn("Could not save {}", path.getFileName(), ex);
                }
            }
        }

        protected void set(String path, Object val) {
            set(path, val, new String[0]);
        }

        @NotNull
        protected String getString(String path, String def) {
            return getString(path, def, new String[0]);
        }

        protected boolean getBoolean(String path, boolean def) {
            return getBoolean(path, def, new String[0]);
        }

        protected double getDouble(String path, double def) {
            return getDouble(path, def, new String[0]);
        }

        protected int getInt(String path, int def) {
            return getInt(path, def, new String[0]);
        }

        protected long getLong(String path, long def) {
            return getLong(path, def, new String[0]);
        }

        @NotNull
        protected <E> List<E> getList(String path, Class<E> type, List<E> def) {
            return getList(path, type, def, new String[0]);
        }

        @NotNull
        protected List<String> getStringList(String path, List<String> def) {
            return getStringList(path, def, new String[0]);
        }

        @NotNull
        protected List<Boolean> getBooleanList(String path, List<Boolean> def) {
            return getBooleanList(path, def, new String[0]);
        }

        @NotNull
        protected List<Float> getFloatList(String path, List<Float> def) {
            return getFloatList(path, def, new String[0]);
        }

        @NotNull
        protected List<Double> getDoubleList(String path, List<Double> def) {
            return getDoubleList(path, def, new String[0]);
        }

        @NotNull
        protected List<Integer> getIntegerList(String path, List<Integer> def) {
            return getIntegerList(path, def, new String[0]);
        }

        @NotNull
        protected List<Long> getLongList(String path, List<Long> def) {
            return getLongList(path, def, new String[0]);
        }

        @NotNull
        protected Map<Object, Object> getMap(String path, Map<Object, Object> def) {
            return getMap(path, def, new String[0]);
        }

        protected CommentedConfigurationNode set(String path, Object val, String... comment) {
            CommentedConfigurationNode node = pathToNode(path);
            try {
                node.set(val);
                comment(node, comment);
            } catch (SerializationException ex) {
                log.error("Could not set {}", path, ex);
            }
            return node;
        }

        protected CommentedConfigurationNode addDefault(
                String path, Object val, String... comment) {
            CommentedConfigurationNode node = pathToNode(path);
            if (node.virtual()) {
                try {
                    node.set(val);
                } catch (SerializationException ex) {
                    log.error("Could not set {}", path, ex);
                }
            }
            comment(node, comment);
            return node;
        }

        @NotNull
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

        @NotNull
        protected List<String> getStringList(String path, List<String> def, String... comment) {
            return getList(path, String.class, def, comment);
        }

        @NotNull
        protected List<Boolean> getBooleanList(String path, List<Boolean> def, String... comment) {
            return getList(path, Boolean.class, def, comment);
        }

        @NotNull
        protected List<Float> getFloatList(String path, List<Float> def, String... comment) {
            return getList(path, Float.class, def, comment);
        }

        @NotNull
        protected List<Double> getDoubleList(String path, List<Double> def, String... comment) {
            return getList(path, Double.class, def, comment);
        }

        @NotNull
        protected List<Integer> getIntegerList(String path, List<Integer> def, String... comment) {
            return getList(path, Integer.class, def, comment);
        }

        @NotNull
        protected List<Long> getLongList(String path, List<Long> def, String... comment) {
            return getList(path, Long.class, def, comment);
        }

        @NotNull
        protected Map<Object, Object> getMap(
                String path, Map<Object, Object> def, String... comment) {
            CommentedConfigurationNode node = addDefault(path, def, comment);
            return toMap(node);
        }

        protected boolean isList(String path) {
            return pathToNode(path).isList();
        }

        protected boolean isMap(String path) {
            return pathToNode(path).isMap();
        }

        protected boolean isNull(String path) {
            return pathToNode(path).isNull();
        }

        @NotNull
        protected Set<String> keys(String path) {
            return keys(pathToNode(path));
        }

        @NotNull
        protected Set<String> keys(String path, String... comment) {
            return keys(pathToNode(path));
        }

        @NotNull
        protected Set<String> keys() {
            return keys(config);
        }

        @NotNull
        protected Set<String> keys(ConfigurationNode node) {
            Set<String> out = new HashSet<>();
            collectKeys(node, "", out);
            return out;
        }

        private void collectKeys(ConfigurationNode node, String prefix, Set<String> out) {
            if (node == null || node.virtual()) return;
            Map<Object, ? extends ConfigurationNode> children = node.childrenMap();
            if (children.isEmpty()) {
                if (!prefix.isEmpty()) out.add(prefix);
                return;
            }
            for (Map.Entry<Object, ? extends ConfigurationNode> e : children.entrySet()) {
                String next =
                        prefix.isEmpty() ? String.valueOf(e.getKey()) : prefix + "." + e.getKey();
                collectKeys(e.getValue(), next, out);
            }
        }

        protected void comment(String path, String... comment) {
            comment(pathToNode(path), comment);
        }

        protected void comment(CommentedConfigurationNode node, String... comment) {
            if (comment.length == 0) return;
            node.comment(String.join("\n", comment));
        }

        @NotNull
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
    }
}
