package kr.rtustudio.framework.bukkit.api.configuration;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.SettingConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
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
import com.google.common.collect.ImmutableMap;

/**
 * 플러그인의 모든 설정 파일을 통합 관리하는 클래스입니다.
 *
 * <p>내부 설정(Setting, Message, Command)과 플러그인별 커스텀 {@link ConfigurationPart}를 등록·로드·리로드합니다.
 * Configurate YAML 로더를 기반으로 동작합니다.
 */
@Slf4j
public class RSConfiguration {

    private final RSPlugin plugin;

    private final Map<Class<? extends ConfigurationPart>, Registry<? extends ConfigurationPart>>
            registries = new HashMap<>();

    private record Registry<C extends ConfigurationPart>(
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> extraSerializer,
            boolean isList,
            Map<String, C> instances) {}

    @Getter private final SettingConfiguration setting;

    @Getter private MessageTranslation message;
    @Getter private CommandTranslation command;

    /**
     * 플러그인의 모든 내부 설정을 초기화한다.
     *
     * @param plugin 이 설정을 소유하는 플러그인
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
     * 단일 {@code .yml} 설정 파일을 등록한다.
     *
     * <p>{@link ConfigPath}의 마지막 요소가 파일명, 나머지가 폴더 경로로 해석된다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 설정 경로 (예: {@code ConfigPath.of("MyConfig")})
     */
    public <C extends ConfigurationPart> C registerConfiguration(
            Class<C> configuration, ConfigPath path) {
        return registerConfiguration(configuration, path, null);
    }

    /**
     * 커스텀 직렬화를 포함하여 단일 {@code .yml} 설정 파일을 등록한다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 설정 경로
     * @param extraSerializer 추가 타입 직렬화
     */
    public <C extends ConfigurationPart> C registerConfiguration(
            Class<C> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> extraSerializer) {
        C instance = registerImpl(configuration, path, path.last(), extraSerializer);

        registries.put(
                configuration,
                new Registry<>(path, extraSerializer, false, Map.of(path.last(), instance)));

        return instance;
    }

    /**
     * 폴더 내 모든 {@code .yml} 파일을 개별 인스턴스로 등록한다.
     *
     * <p>{@link ConfigPath}의 전체 요소가 폴더 경로로 해석된다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 폴더 경로 (예: {@code ConfigPath.of("Regions")})
     * @return 파일명(확장자 제외)을 키로 하는 {@link ConfigList}
     */
    public <C extends ConfigurationPart> ConfigList<C> registerConfigurations(
            Class<C> configuration, ConfigPath path) {
        return registerConfigurations(configuration, path, null);
    }

    /**
     * 커스텀 직렬화를 포함하여 폴더 내 모든 {@code .yml} 파일을 등록한다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 폴더 경로
     * @param extraSerializer 추가 타입 직렬화
     * @return 파일명(확장자 제외)을 키로 하는 {@link ConfigList}
     */
    public <C extends ConfigurationPart> ConfigList<C> registerConfigurations(
            Class<C> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> extraSerializer) {
        String folderPath = path.folderPath();
        Path configFolder = plugin.getDataFolder().toPath().resolve(folderPath);
        Map<String, C> result = new LinkedHashMap<>();
        try {
            if (Files.notExists(configFolder)) Files.createDirectories(configFolder);
            try (java.util.stream.Stream<Path> stream = Files.list(configFolder)) {
                stream.filter(p -> p.toString().endsWith(".yml"))
                        .sorted()
                        .forEach(
                                file -> {
                                    String name = file.getFileName().toString();
                                    String key = name.replace(".yml", "");
                                    C instance = registerImpl(configuration, path, name, extraSerializer);
                                    result.put(key, instance);
                                });
            }
        } catch (IOException e) {
            log.warn("Could not scan folder {}", folderPath, e);
        }
        registries.put(configuration, new Registry<>(path, extraSerializer, true, result));
        return new ConfigList<>(result);
    }

    private <C extends ConfigurationPart> C registerImpl(
            Class<C> configuration,
            ConfigPath path,
            String name,
            Consumer<TypeSerializerCollection.Builder> extraSerializer) {
        String folder = path.folderPath();
        Integer version = path.version();
        name = name.endsWith(".yml") ? name : name + ".yml";
        Path configFolder = plugin.getDataFolder().toPath().resolve(folder);
        Path configFile = configFolder.resolve(name);
        BufferedReader defaultConfig = null;
        try {
            InputStream in = plugin.getResource(folder + "/" + name);
            if (in != null) {
                defaultConfig =
                        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
        PluginConfiguration<C> pluginConfiguration;
        try {
            pluginConfiguration =
                    new PluginConfiguration<>(
                            plugin,
                            configuration,
                            configFile,
                            defaultConfig,
                            version,
                            extraSerializer);

        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        return pluginConfiguration.load();
    }

    /**
     * 등록된 설정 인스턴스를 타입으로 조회한다.
     *
     * @param configuration 조회할 {@link ConfigurationPart} 클래스
     * @param <C> 설정 타입
     * @return 등록된 설정 인스턴스, 없으면 {@code null}
     */
    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> C get(Class<C> configuration) {
        Registry<C> registry = (Registry<C>) this.registries.get(configuration);
        if (registry == null || registry.instances().isEmpty()) return null;
        return registry.instances().values().iterator().next();
    }

    /**
     * 등록된 설정 목록 인스턴스를 타입으로 조회한다.
     *
     * @param configuration 조회할 {@link ConfigurationPart} 클래스
     * @param <C> 설정 타입
     * @return 등록된 설정 목록 인스턴스, 없으면 {@code null}
     */
    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> ConfigList<C> getList(Class<C> configuration) {
        Registry<C> registry = (Registry<C>) this.registries.get(configuration);
        if (registry == null) return null;
        return new ConfigList<>(registry.instances());
    }

    /**
     * 내부 설정(Setting, Storage, Message, Command)을 모두 리로드한다.
     *
     * <p>로케일이 변경된 경우 번역 객체를 새로 생성한다.
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

    /** 등록된 모든 커스텀 설정을 파일에서 다시 로드한다. */
    public void reloadAll() {
        for (Class<? extends ConfigurationPart> configuration : this.registries.keySet())
            reload(configuration);
    }

    /**
     * 지정한 설정을 파일에서 다시 로드한다. 단일 설정뿐만 아니라 폴더로 등록된 설정 목록도 함께 리로드하며, 폴더의 경우 새로 추가/삭제된 파일까지 모두 반영하여
     * 갱신합니다.
     *
     * @param configuration 리로드할 {@link ConfigurationPart} 클래스
     * @param <C> 설정 타입
     * @return 리로드 성공 여부
     */
    @SuppressWarnings("unchecked")
    public <C extends ConfigurationPart> boolean reload(Class<C> configuration) {
        Registry<C> registry = (Registry<C>) this.registries.get(configuration);
        if (registry == null) return false;

        try {
            if (registry.isList()) {
                registerConfigurations(configuration, registry.path(), registry.extraSerializer());
            } else {
                registerConfiguration(configuration, registry.path(), registry.extraSerializer());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * YAML 설정 파일 하나를 관리하는 래퍼 클래스입니다.
     *
     * <p>Configurate {@link YamlConfigurationLoader}를 기반으로 노드 읽기/쓰기, 기본값 처리, 변경 감지 등을 제공합니다. 하위
     * 클래스에서 {@code private void} 메서드를 선언하면 {@link #setup} 및 {@link #reload} 시 자동으로 호출됩니다.
     *
     * @param <T> 소유 플러그인 타입
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
            this(plugin, path.folder(), path.last(), path.version());
        }

        public Wrapper(T plugin, String folder, String name, Integer version) {
            this.plugin = plugin;
            String id = plugin.getName();
            String author = String.join(" & ", plugin.getDescription().getAuthors());
            String header = String.format(PluginConfiguration.HEADER, id, author, id, author);
            this.path =
                    plugin.getDataFolder()
                            .toPath()
                            .resolve(folder)
                            .resolve(name.endsWith(".yml") ? name : name + ".yml");
            YamlConfigurationLoader.Builder builder =
                    YamlConfigurationLoader.builder()
                            .path(path)
                            .indent(2)
                            .nodeStyle(NodeStyle.BLOCK)
                            .headerMode(HeaderMode.PRESERVE)
                            .defaultOptions(
                                    co ->
                                            ConfigurationSerializer.apply(
                                                    co.header(header).shouldCopyDefaults(true)));
            final BufferedReader defaultConfig = configFromResource(folder, name);
            if (defaultConfig == null || Files.exists(this.path)) this.loader = builder.build();
            else this.loader = builder.source(() -> defaultConfig).build();
            this.version = version != null ? version : 0;
            try {
                if (Files.notExists(path)) {
                    if (defaultConfig == null) {
                        this.config = CommentedConfigurationNode.root(this.loader.defaultOptions());
                    } else this.config = this.loader.load();
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
                log.warn("Could not initialize {}", folder + "/" + name, e);
                throw new RuntimeException(e);
            }
        }

        private BufferedReader configFromResource(String folder, String name) {
            BufferedReader result = null;
            try {
                InputStream in = plugin.getResource(folder + "/" + name);
                if (in != null)
                    result = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
            return result;
        }

        protected Map<Object, Object> toMap(@NotNull ConfigurationNode node) {
            ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry :
                    node.childrenMap().entrySet()) {
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
                if (Modifier.isPrivate(method.getModifiers())) {
                    if (method.getParameterTypes().length == 0
                            && method.getReturnType() == Void.TYPE) {
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

        @NotNull
        protected String getString(String path, String def) {
            return getString(path, def, new String[] {});
        }

        protected boolean getBoolean(String path, boolean def) {
            return getBoolean(path, def, new String[] {});
        }

        protected double getDouble(String path, double def) {
            return getDouble(path, def, new String[] {});
        }

        protected int getInt(String path, int def) {
            return getInt(path, def, new String[] {});
        }

        protected long getLong(String path, long def) {
            return getLong(path, def, new String[] {});
        }

        @NotNull
        protected <E> List<E> getList(String path, Class<E> type, List<E> def) {
            return getList(path, type, def, new String[] {});
        }

        @NotNull
        protected List<String> getStringList(String path, List<String> def) {
            return getStringList(path, def, new String[] {});
        }

        @NotNull
        protected List<Boolean> getBooleanList(String path, List<Boolean> def) {
            return getBooleanList(path, def, new String[] {});
        }

        @NotNull
        protected List<Float> getFloatList(String path, List<Float> def) {
            return getFloatList(path, def, new String[] {});
        }

        @NotNull
        protected List<Double> getDoubleList(String path, List<Double> def) {
            return getDoubleList(path, def, new String[] {});
        }

        @NotNull
        protected List<Integer> getIntegerList(String path, List<Integer> def) {
            return getIntegerList(path, def, new String[] {});
        }

        @NotNull
        protected List<Long> getLongList(String path, List<Long> def) {
            return getLongList(path, def, new String[] {});
        }

        @NotNull
        protected Map<Object, Object> getMap(String path, Map<Object, Object> def) {
            return getMap(path, def, new String[] {});
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

        protected CommentedConfigurationNode addDefault(
                String path, Object val, String... comment) {
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
            CommentedConfigurationNode node = pathToNode(path);
            return node.isList();
        }

        protected boolean isMap(String path) {
            CommentedConfigurationNode node = pathToNode(path);
            return node.isMap();
        }

        protected boolean isNull(String path) {
            CommentedConfigurationNode node = pathToNode(path);
            if (node == null) return true;
            return node.isNull();
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
            if (children == null || children.isEmpty()) {
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
            CommentedConfigurationNode node = pathToNode(path);
            comment(node, comment);
        }

        protected void comment(CommentedConfigurationNode node, String... comment) {
            if (comment.length == 0) return;
            node.comment(String.join("\n", comment));
            try {
                loader.save(node);
            } catch (ConfigurateException ex) {
                throw new RuntimeException(ex);
            }
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

        private Long parseLong(String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
