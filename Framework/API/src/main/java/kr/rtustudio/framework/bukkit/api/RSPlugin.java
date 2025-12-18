package kr.rtustudio.framework.bukkit.api;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.core.module.ThemeModule;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.integration.Integration;
import kr.rtustudio.framework.bukkit.api.library.LibraryLoader;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.framework.bukkit.api.storage.Storage;
import kr.rtustudio.protoweaver.api.ProtoConnectionHandler;
import kr.rtustudio.protoweaver.api.callback.HandlerCallback;
import kr.rtustudio.protoweaver.api.protocol.Packet;
import lombok.Getter;
import lombok.Setter;
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
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.google.gson.JsonObject;

@Getter
@SuppressWarnings("unused")
public abstract class RSPlugin extends JavaPlugin {

    private static final String MINIMUM_SUPPORTED_VERSION = "1.20.1";

    private final Set<RSListener<? extends RSPlugin>> listeners = new HashSet<>();
    private final Set<RSCommand<? extends RSPlugin>> commands = new HashSet<>();
    private final Set<Integration> integrations = new HashSet<>();
    private final LinkedHashSet<String> languages = new LinkedHashSet<>();

    @Getter private final LibraryLoader libraryLoader;

    private Framework framework;
    private Component prefix;
    private RSPlugin plugin;
    private BukkitAudiences adventure;
    private RSConfiguration configuration;
    @Setter private Storage storage;

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
        if (MinecraftVersion.isSupport(MINIMUM_SUPPORTED_VERSION)) {
            this.plugin = this;
            this.adventure = BukkitAudiences.create(this);
        } else {
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

        registerPermission("command.reload", PermissionDefault.OP);
        for (String plugin : this.getDescription().getSoftDepend())
            this.framework.hookDependency(plugin);
        enable();
        console("<green>Enable!</green>");
        this.framework.loadPlugin(this);
    }

    @Override
    public void onDisable() {
        this.integrations.forEach(Integration::unregister);
        disable();
        if (this.storage != null) this.storage.close();
        this.framework.unloadPlugin(this);
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
        ThemeModule theme = this.framework.getModules().getTheme();
        String text =
                String.format(
                        "<gradient:%s:%s>%s%s%s</gradient>",
                        theme.getGradientStart(),
                        theme.getGradientEnd(),
                        theme.getPrefix(),
                        getName(),
                        theme.getSuffix());
        this.prefix = ComponentFormatter.mini(text);
        load();
    }

    public void verbose(Component message) {
        if (this.configuration.getSetting().isVerbose()) console(message);
    }

    public void verbose(String minimessage) {
        verbose(ComponentFormatter.mini(minimessage));
    }

    public void console(Component message) {
        getAdventure().console().sendMessage(getPrefix().append(message));
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

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, String name) {
        return this.configuration.register(configuration, name);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, String name, Integer version) {
        return this.configuration.register(configuration, name, version);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            String name,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.register(configuration, name, null, serializers);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            String name,
            Integer version,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.register(configuration, name, version, serializers);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, String folder, String name) {
        return this.configuration.register(configuration, folder, name);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, String folder, String name, Integer version) {
        return this.configuration.register(configuration, folder, name, version);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            String folder,
            String name,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.register(configuration, folder, name, null, serializers);
    }

    protected <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            String folder,
            String name,
            Integer version,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.register(configuration, folder, name, version, serializers);
    }

    protected void initStorage(String... storages) {
        this.configuration.getStorage().init(storages);
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

    /**
     * 프록시의 RSFramework와 통신을 위한 프로토콜 등록
     *
     * @param namespace 네임스페이스
     * @param key 키
     * @param packet 패킷 정보
     * @param protocolHandler 수신을 담당하는 핸들러
     * @param callback 핸들러 외부에서 수신 이벤트를 받는 callback
     */
    protected void registerProtocol(
            String namespace,
            String key,
            Packet packet,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback) {
        this.framework.registerProtocol(namespace, key, packet, protocolHandler, callback);
    }

    /**
     * 프록시의 RSFramework와 통신을 위한 프로토콜 등록
     *
     * @param namespace 네임스페이스
     * @param key 키
     * @param packets 패킷 정보
     * @param protocolHandler 수신을 담당하는 핸들러
     * @param callback 핸들러 외부에서 수신 이벤트를 받는 callback
     */
    protected void registerProtocol(
            String namespace,
            String key,
            Set<Packet> packets,
            Class<? extends ProtoConnectionHandler> protocolHandler,
            HandlerCallback callback) {
        this.framework.registerProtocol(namespace, key, packets, protocolHandler, callback);
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

    public void syncStorage(String name, JsonObject json) {}
}
