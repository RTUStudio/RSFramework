package kr.rtustudio.framework.bukkit.api;

import kr.rtustudio.broker.Broker;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigList;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.StorageConfiguration;
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
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

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

    public RSPlugin() {
        this("en_us", "ko_kr");
    }

    public RSPlugin(String... languages) {
        Collections.addAll(this.languages, languages);
        this.libraryLoader = new LibraryLoader(this);
    }

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
        for (String dependency : this.getDescription().getSoftDepend())
            this.framework.hookDependency(dependency);
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
        if (this.adventure == null) return;
        this.adventure.close();
        this.adventure = null;
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

    public void verbose(Component message) {
        if (this.configuration.getSetting().isVerbose()) console(message);
    }

    public void verbose(String minimessage) {
        verbose(ComponentFormatter.mini(minimessage));
    }

    public void console(Component message) {
        if (this.adventure == null) return;
        this.adventure.console().sendMessage(getPrefix().append(message));
    }

    public void console(String minimessage) {
        console(ComponentFormatter.mini(minimessage));
    }

    public void loadLibrary(String dependency) {
        this.libraryLoader.load(dependency);
    }

    public void loadLibrary(String dependency, String pattern, String relocatedPattern) {
        this.libraryLoader.load(dependency, pattern, relocatedPattern);
    }

    public <T extends ConfigurationPart> T getConfiguration(Class<T> configuration) {
        return this.configuration.get(configuration);
    }

    public boolean reloadConfiguration(Class<? extends ConfigurationPart> configuration) {
        return this.configuration.reload(configuration);
    }

    public <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, ConfigPath path) {
        return this.configuration.registerConfiguration(configuration, path);
    }

    public <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.registerConfiguration(configuration, path, serializers);
    }

    public <T extends ConfigurationPart> ConfigList<T> registerConfigurations(
            Class<T> configuration, ConfigPath path) {
        return this.configuration.registerConfigurations(configuration, path);
    }

    public <T extends ConfigurationPart> ConfigList<T> registerConfigurations(
            Class<T> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.registerConfigurations(configuration, path, serializers);
    }

    /**
     * 등록된 스토리지 인스턴스를 반환한다.
     *
     * @param name 스토리지 식별 이름 (예: {@code "Local"}, {@code "LocalSQL"})
     * @return 해당 이름의 {@link Storage}, 등록되지 않았으면 {@code null}
     * @see StorageConfiguration#getStorage(String)
     */
    public Storage getStorage(@NotNull String name) {
        return this.configuration.getStorage().getStorage(name);
    }

    /**
     * 지정한 이름과 타입으로 스토리지를 등록한다.
     *
     * <p>{@code Storage.yml}에 해당 name이 없으면 새로 추가하고, 이미 존재하면 yml에 기록된 타입을 우선 사용한다.
     *
     * <pre>{@code
     * registerStorage("LocalSQL", StorageType.SQLITE);
     * registerStorage("Local", StorageType.JSON);
     * }</pre>
     *
     * @param name 스토리지 식별 이름
     * @param type 기본 스토리지 타입
     * @see StorageConfiguration#registerStorage(String, StorageType)
     */
    protected void registerStorage(@NotNull String name, @NotNull StorageType type) {
        this.configuration.getStorage().registerStorage(name, type);
    }

    /**
     * 지정한 이름으로 스토리지를 등록한다. 기본 타입은 {@link StorageType#JSON}.
     *
     * @param name 스토리지 식별 이름
     * @see #registerStorage(String, StorageType)
     */
    protected void registerStorage(@NotNull String name) {
        this.configuration.getStorage().registerStorage(name);
    }

    public <T extends Broker> T getBroker(@NotNull Class<T> type) {
        return this.framework.getBroker(type);
    }

    public <T extends Provider> T getProvider(@NotNull Class<T> type) {
        return this.framework.getProvider(type);
    }

    public <T extends Provider> void setProvider(@NotNull Class<T> type, @NotNull T provider) {
        this.framework.setProvider(type, provider);
    }

    protected void registerEvent(RSListener<? extends RSPlugin> listener) {
        this.listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void registerEvents() {
        for (Listener listener : this.listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    public void unregisterEvents() {
        for (HandlerList handler : HandlerList.getHandlerLists()) {
            handler.unregister(this);
        }
    }

    protected void registerCommand(RSCommand<? extends RSPlugin> command) {
        registerCommand(command, false);
    }

    protected void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload) {
        this.commands.add(command);
        this.framework.registerCommand(command, reload);
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(getName().toLowerCase() + "." + permission.toLowerCase());
    }

    public void registerPermission(String permission, PermissionDefault permissionDefault) {
        String name = getName() + "." + permission;
        this.framework.registerPermission(name.toLowerCase(), permissionDefault);
    }

    protected void registerIntegration(Integration integrationWrapper) {
        if (!integrationWrapper.isAvailable()) return;
        this.integrations.add(integrationWrapper);
        integrationWrapper.register();
    }

    protected void initialize() {}

    protected void load() {}

    protected void enable() {}

    protected void disable() {}
}
