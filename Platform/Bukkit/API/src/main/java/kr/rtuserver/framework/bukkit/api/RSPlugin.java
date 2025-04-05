package kr.rtuserver.framework.bukkit.api;

import com.google.gson.JsonObject;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.configuration.impl.Configurations;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.core.module.ThemeModule;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.utility.platform.MinecraftVersion;
import kr.rtuserver.protoweaver.api.ProtoConnectionHandler;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class RSPlugin extends JavaPlugin {

    @Getter
    private final Set<RSListener<? extends RSPlugin>> listeners = new HashSet<>();
    @Getter
    private final Set<RSCommand<? extends RSPlugin>> commands = new HashSet<>();
    @Getter
    private final Set<String> languages = new HashSet<>();
    @Getter
    private Framework framework;
    private Component prefix;
    @Getter
    private RSPlugin plugin;
    @Getter
    private BukkitAudiences adventure;
    @Getter
    private Configurations configurations;
    @Getter
    @Setter
    private Storage storage;

    public RSPlugin() {
        this("ko_kr", "en_us");
    }

    public RSPlugin(String... languages) {
        this.languages.addAll(Set.of(languages));
    }

    public Component getPrefix() {
        String str = configurations.getSetting().getPrefix();
        if (str.isEmpty()) return prefix;
        return ComponentFormatter.mini(str);
    }

    @Override
    public void onEnable() {
        if (MinecraftVersion.isSupport("1.17.1")) {
            plugin = this;
            adventure = BukkitAudiences.create(this);
        } else {
            Bukkit.getLogger().warning("Server version is unsupported version (< 1.17.1), Disabling this plugin...");
            Bukkit.getLogger().warning("서버 버전이 지원되지 않는 버전입니다 (< 1.17.1), 플러그인을 비활성화합니다...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        registerPermission(plugin.getName() + ".reload", PermissionDefault.OP);
        for (String plugin : this.getDescription().getSoftDepend()) framework.hookDependency(plugin);
        configurations = new Configurations(this);
        enable();
        console("<green>Enable!</green>");
        framework.loadPlugin(this);
    }

    @Override
    public void onDisable() {
        disable();
        if (storage != null) storage.close();
        framework.unloadPlugin(this);
        console("<red>Disable!</red>");
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @Override
    public void onLoad() {
        this.framework = LightDI.getBean(Framework.class);
        initialize();
        ThemeModule theme = this.framework.getModules().getThemeModule();
        String text = String.format("<gradient:%s:%s>%s%s%s</gradient>",
                theme.getGradientStart(),
                theme.getGradientEnd(),
                theme.getPrefix(),
                getName(),
                theme.getSuffix());
        this.prefix = ComponentFormatter.mini(text);
        load();
    }

    public void console(Component message) {
        getAdventure().console().sendMessage(getPrefix().append(message));
    }

    public void console(String minimessage) {
        getAdventure().console().sendMessage(getPrefix().append(ComponentFormatter.mini(minimessage)));
    }

    public void registerEvent(RSListener<? extends RSPlugin> listener) {
        this.listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }


    public void registerEvents() {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    public void unregisterEvents() {
        for (HandlerList handler : HandlerList.getHandlerLists()) {
            handler.unregister(this);
        }
    }

    public void registerCommand(RSCommand<? extends RSPlugin> command) {
        registerCommand(command, false);
    }

    public void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload) {
        this.commands.add(command);
        framework.registerCommand(command, reload);
    }

    public void registerPermission(String name, PermissionDefault permissionDefault) {
        framework.registerPermission(name, permissionDefault);
    }

    /**
     * 프록시의 RSFramework과 통신을 위한 프로토콜 등록
     *
     * @param namespace       네임스페이스
     * @param key             키
     * @param packet          패킷 정보
     * @param protocolHandler 수신을 담당하는 핸들러
     * @param callback        핸들러 외부에서 수신 이벤트를 받는 callback
     */
    protected void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        framework.registerProtocol(namespace, key, packet, protocolHandler, callback);
    }

    protected void initialize() {
    }

    protected void load() {
    }

    protected void enable() {
    }

    protected void disable() {
    }

    public void syncStorage(String name, JsonObject json) {
    }

}