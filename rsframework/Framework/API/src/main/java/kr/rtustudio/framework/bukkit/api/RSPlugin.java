package kr.rtustudio.framework.bukkit.api;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.configurate.model.ConfigList;
import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.core.module.ThemeModule;
import kr.rtustudio.framework.bukkit.api.core.provider.Provider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.integration.Integration;
import kr.rtustudio.framework.bukkit.api.library.LibraryLoader;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

/**
 * Manages command, event, configuration, storage, and module registration and lifecycles. Extends
 * Bukkit {@link JavaPlugin} and provides {@link #initialize()}, {@link #load()}, {@link #enable()},
 * {@link #disable()} callbacks for plugin lifecycle hooks.
 *
 * <p>플러그인의 메인 클래스가 상속해야 하는 추상 클래스. 명령어, 이벤트, 설정, 스토리지 및 각종 모듈의 등록과 생명주기를 관리한다.
 */
@Getter
@SuppressWarnings("unused")
public abstract class RSPlugin extends JavaPlugin {

    private static final String MINIMUM_SUPPORTED_VERSION = "1.20.1";

    private final Set<RSListener<? extends RSPlugin>> listeners = new HashSet<>();
    private final Set<RSCommand<? extends RSPlugin>> commands = new HashSet<>();
    private final Set<Integration> integrations = new HashSet<>();
    private final LinkedHashSet<String> languages = new LinkedHashSet<>();

    private final LibraryLoader libraryLoader;

    private Framework framework;
    private Component prefix;
    private BukkitAudiences adventure;
    private RSConfiguration configuration;

    /**
     * Creates a plugin with default locales ({@code en_us}, {@code ko_kr}).
     *
     * <p>기본 로케일({@code en_us}, {@code ko_kr})로 플러그인을 생성한다.
     */
    public RSPlugin() {
        this("en_us", "ko_kr");
    }

    /**
     * Creates a plugin with the specified locale list.
     *
     * <p>지정한 로케일 목록으로 플러그인을 생성한다.
     *
     * @param languages locale codes to support (first is the default locale)
     */
    public RSPlugin(String... languages) {
        Collections.addAll(this.languages, languages);
        this.libraryLoader = new LibraryLoader(this);
    }

    /**
     * Returns the plugin's prefix component loaded from the configuration file.
     *
     * <p>설정 파일에서 불러온 플러그인의 접두사 컴포넌트를 반환한다.
     */
    public Component getPrefix() {
        String str = this.configuration.getSetting().getPrefix();
        if (str.isEmpty()) return this.prefix;
        return ComponentFormatter.mini(str);
    }

    @Override
    public void onEnable() {
        if (!MinecraftVersion.isSupport(MINIMUM_SUPPORTED_VERSION)) {
            Bukkit.getLogger()
                    .warning(
                            "Server version is unsupported version (< "
                                    + MINIMUM_SUPPORTED_VERSION
                                    + "), Disabling this plugin...");
            Bukkit.getLogger()
                    .warning(
                            "서버 버전이 지원되지 않는 버전입니다 (< "
                                    + MINIMUM_SUPPORTED_VERSION
                                    + "), 플러그인을 비활성화합니다...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.adventure = BukkitAudiences.create(this);
        registerPermission("command.reload", PermissionDefault.OP);
        this.getDescription().getSoftDepend().forEach(this.framework::hookDependency);
        enable();
        console("<green>Enable!</green>");
        this.framework.loadPlugin(this);
    }

    @Override
    public void onDisable() {
        this.integrations.forEach(Integration::unregister);
        disable();
        if (this.framework != null) this.framework.unloadPlugin(this);
        console("<red>Disable!</red>");
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public void onLoad() {
        this.framework = LightDI.getBean(Framework.class);
        this.configuration = new RSConfiguration(this);
        initialize();
        ThemeModule theme = this.framework.getModule(ThemeModule.class);
        this.prefix =
                ComponentFormatter.mini(
                        "<gradient:%s:%s>%s%s%s</gradient>"
                                .formatted(
                                        theme.getGradientStart(),
                                        theme.getGradientEnd(),
                                        theme.getPrefix(),
                                        getName(),
                                        theme.getSuffix()));
        load();
    }

    /**
     * Prints a message to console only when verbose (debug) mode is enabled.
     *
     * <p>디버그(verbose) 모드일 때만 콘솔에 메시지를 출력한다.
     *
     * @param message component to print
     */
    public void verbose(Component message) {
        if (this.configuration.getSetting().isVerbose()) console(message);
    }

    /**
     * Prints a MiniMessage-formatted message to console only when verbose mode is enabled.
     *
     * <p>디버그(verbose) 모드일 때만 콘솔에 MiniMessage 형식 메시지를 출력한다.
     *
     * @param minimessage MiniMessage format string
     */
    public void verbose(String minimessage) {
        verbose(ComponentFormatter.mini(minimessage));
    }

    /**
     * Prints a message to console with the plugin prefix.
     *
     * <p>플러그인 접두사와 함께 콘솔에 메시지를 출력한다.
     *
     * @param message component to print
     */
    public void console(Component message) {
        if (this.adventure != null) {
            this.adventure.console().sendMessage(getPrefix().append(message));
        }
    }

    /**
     * Prints a MiniMessage-formatted message to console with the plugin prefix.
     *
     * <p>플러그인 접두사와 함께 콘솔에 MiniMessage 형식 메시지를 출력한다.
     *
     * @param minimessage MiniMessage format string
     */
    public void console(String minimessage) {
        console(ComponentFormatter.mini(minimessage));
    }

    /**
     * Dynamically loads a Maven library at runtime.
     *
     * <p>Maven 형식의 라이브러리를 런타임에 동적으로 로드한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} format string
     */
    public void loadLibrary(String dependency) {
        this.libraryLoader.load(dependency);
    }

    /**
     * Loads a library at runtime with package relocation applied.
     *
     * <p>패키지 재배치(relocation)를 적용하여 라이브러리를 런타임에 로드한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} format string
     * @param pattern original package pattern
     * @param relocatedPattern target relocated package pattern
     */
    public void loadLibrary(String dependency, String pattern, String relocatedPattern) {
        this.libraryLoader.load(dependency, pattern, relocatedPattern);
    }

    /**
     * Retrieves a registered configuration instance by type.
     *
     * <p>등록된 설정 인스턴스를 타입으로 조회한다.
     *
     * @param configuration {@link ConfigurationPart} class to look up
     * @param <T> configuration type
     * @return registered instance, or {@code null} if not found
     */
    public <T extends ConfigurationPart> T getConfiguration(Class<T> configuration) {
        return this.configuration.get(configuration);
    }

    /**
     * Retrieves a registered configuration list instance by type.
     *
     * <p>등록된 설정 목록 인스턴스를 타입으로 조회한다.
     *
     * @param configuration {@link ConfigurationPart} class to look up
     * @param <T> configuration type
     * @return registered config list, or empty {@link ConfigList} if not found
     */
    public <T extends ConfigurationPart> ConfigList<T> getConfigurations(Class<T> configuration) {
        return this.configuration.getList(configuration);
    }

    /**
     * Reloads the specified configuration from file.
     *
     * <p>지정한 설정을 파일에서 다시 로드한다.
     *
     * @param configuration {@link ConfigurationPart} class to reload
     * @return whether reload succeeded
     */
    public boolean reloadConfiguration(Class<? extends ConfigurationPart> configuration) {
        return this.configuration.reload(configuration);
    }

    /**
     * Registers and loads a single YAML configuration file.
     *
     * <p>단일 YAML 설정 파일을 등록하고 로드한다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path configuration path
     * @param <T> configuration type
     * @return loaded configuration instance
     */
    public <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, ConfigPath path) {
        return this.configuration.registerConfiguration(configuration, path);
    }

    /**
     * Registers and loads a single YAML configuration file with custom serializers.
     *
     * <p>커스텀 직렬화를 포함하여 단일 YAML 설정 파일을 등록하고 로드한다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path configuration path
     * @param serializers additional type serializers
     * @param <T> configuration type
     * @return loaded configuration instance
     */
    public <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.registerConfiguration(configuration, path, serializers);
    }

    /**
     * Registers all YAML files in a directory as individual configuration instances.
     *
     * <p>폴더 내 모든 YAML 파일을 개별 설정 인스턴스로 등록한다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path directory path
     * @param <T> configuration type
     * @return {@link ConfigList} keyed by filename
     */
    public <T extends ConfigurationPart> ConfigList<T> registerConfigurations(
            Class<T> configuration, ConfigPath path) {
        return this.configuration.registerConfigurations(configuration, path);
    }

    /**
     * Registers all YAML files in a directory with custom serializers.
     *
     * <p>커스텀 직렬화를 포함하여 폴더 내 모든 YAML 파일을 등록한다.
     *
     * @param configuration {@link ConfigurationPart} class to load
     * @param path directory path
     * @param serializers additional type serializers
     * @param <T> configuration type
     * @return {@link ConfigList} keyed by filename
     */
    public <T extends ConfigurationPart> ConfigList<T> registerConfigurations(
            Class<T> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.registerConfigurations(configuration, path, serializers);
    }

    /**
     * Returns a registered storage instance.
     *
     * <p>등록된 스토리지 인스턴스를 반환한다.
     *
     * @param name storage identifier (e.g. {@code "Local"}, {@code "LocalSQL"})
     * @return the {@link Storage}, or {@code null} if not registered
     */
    public Storage getStorage(@NotNull String name) {
        return this.framework.getStorage(this, name);
    }

    /**
     * Registers a storage with the specified name and type.
     *
     * <p>지정한 이름과 타입으로 스토리지를 등록한다.
     *
     * <pre>{@code
     * registerStorage("LocalSQL", StorageType.SQLITE);
     * registerStorage("Local", StorageType.JSON);
     * }</pre>
     *
     * @param name storage identifier
     * @param type default storage type
     */
    public void registerStorage(@NotNull String name, @NotNull StorageType type) {
        this.framework.registerStorage(this, name, type);
    }

    /**
     * Registers a storage with the specified name. Default type is {@link StorageType#JSON}.
     *
     * <p>지정한 이름으로 스토리지를 등록한다. 기본 타입은 {@link StorageType#JSON}.
     *
     * @param name storage identifier
     */
    public void registerStorage(@NotNull String name) {
        this.framework.registerStorage(this, name, StorageType.JSON);
    }

    /**
     * Retrieves a registered bridge instance by type.
     *
     * <p>등록된 브릿지 인스턴스를 타입으로 조회한다.
     *
     * @param type bridge interface class
     * @param <T> bridge type
     * @return registered bridge instance
     */
    public <T extends Bridge> T getBridge(@NotNull Class<T> type) {
        return this.framework.getBridge(type);
    }

    /**
     * Retrieves a registered provider instance by type.
     *
     * <p>등록된 프로바이더 인스턴스를 타입으로 조회한다.
     *
     * @param type provider interface class
     * @param <T> provider type
     * @return registered provider instance, or {@code null}
     */
    public <T extends Provider> T getProvider(@NotNull Class<T> type) {
        return this.framework.getProvider(type);
    }

    /**
     * Registers or replaces a provider instance.
     *
     * <p>프로바이더 인스턴스를 등록하거나 교체한다.
     *
     * @param type provider interface class
     * @param provider provider instance to register
     * @param <T> provider type
     */
    public <T extends Provider> void setProvider(@NotNull Class<T> type, @NotNull T provider) {
        this.framework.setProvider(type, provider);
    }

    /**
     * Registers an event listener.
     *
     * <p>이벤트 리스너를 등록한다.
     *
     * @param listener {@link RSListener} to register
     */
    public void registerEvent(RSListener<? extends RSPlugin> listener) {
        this.listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /**
     * Re-registers all registered event listeners.
     *
     * <p>등록된 모든 이벤트 리스너를 다시 활성화한다.
     */
    public void registerEvents() {
        this.listeners.forEach(
                listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    /**
     * Unregisters all event listeners for this plugin.
     *
     * <p>이 플러그인에 등록된 모든 이벤트 리스너를 해제한다.
     */
    public void unregisterEvents() {
        HandlerList.getHandlerLists().forEach(handler -> handler.unregister(this));
    }

    /**
     * Registers a command.
     *
     * <p>명령어를 등록한다.
     *
     * @param command {@link RSCommand} to register
     */
    public void registerCommand(RSCommand<? extends RSPlugin> command) {
        registerCommand(command, false);
    }

    /**
     * Registers a command.
     *
     * <p>명령어를 등록한다.
     *
     * @param command {@link RSCommand} to register
     * @param reload if {@code true}, immediately refreshes the server command map
     */
    public void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload) {
        this.commands.add(command);
        this.framework.registerCommand(command, reload);
    }

    /**
     * Checks whether the sender has a specific permission for this plugin. The permission node is
     * assembled as {@code <pluginName>.<permission>}.
     *
     * <p>발신자가 이 플러그인의 특정 권한을 가지고 있는지 확인한다.
     *
     * @param sender target to check
     * @param permission permission suffix (e.g. {@code "command.reload"})
     * @return whether the sender has the permission
     */
    public boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(getName().toLowerCase() + "." + permission.toLowerCase());
    }

    /**
     * Registers a permission node for this plugin on the server.
     *
     * <p>이 플러그인의 권한 노드를 서버에 등록한다.
     *
     * @param permission permission suffix (e.g. {@code "command.reload"})
     * @param permissionDefault default permission level
     */
    public void registerPermission(String permission, PermissionDefault permissionDefault) {
        String name = getName() + "." + permission;
        this.framework.registerPermission(name.toLowerCase(), permissionDefault);
    }

    /**
     * Registers an external plugin integration. Ignored if the integration target is unavailable.
     *
     * <p>외부 플러그인 연동(Integration)을 등록한다. 연동 대상이 사용 불가능하면 무시된다.
     *
     * @param integrationWrapper integration wrapper to register
     */
    public void registerIntegration(Integration integrationWrapper) {
        if (!integrationWrapper.isAvailable()) return;
        this.integrations.add(integrationWrapper);
        integrationWrapper.register();
    }

    /**
     * Plugin initialization callback. Called early in {@code onLoad()}. Implement library loading,
     * configuration registration, etc. here.
     *
     * <p>플러그인 초기화 콜백. {@code onLoad()} 초반에 호출된다. 라이브러리 로드, 설정 등록 등 프레임워크 초기화 전에 수행할 작업을 여기에 구현한다.
     */
    protected void initialize() {}

    /**
     * Plugin load callback. Called at the end of {@code onLoad()}. Implement tasks to perform after
     * framework initialization here.
     *
     * <p>플러그인 로드 콜백. {@code onLoad()} 후반에 호출된다. 프레임워크 초기화가 완료된 후 수행할 작업을 여기에 구현한다.
     */
    protected void load() {}

    /**
     * Plugin enable callback. Called inside {@code onEnable()}. Implement command, event, and
     * storage registration here.
     *
     * <p>플러그인 활성화 콜백. {@code onEnable()} 내부에서 호출된다. 명령어, 이벤트, 스토리지 등록 등을 여기에 구현한다.
     */
    protected void enable() {}

    /**
     * Plugin disable callback. Called inside {@code onDisable()}. Implement resource cleanup and
     * shutdown tasks here.
     *
     * <p>플러그인 비활성화 콜백. {@code onDisable()} 내부에서 호출된다. 리소스 정리 등 종료 작업을 여기에 구현한다.
     */
    protected void disable() {}
}
