package kr.rtustudio.framework.bukkit.core;

import de.tr7zw.changeme.nbtapi.NBT;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.BridgeRegistry;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.protocol.internal.BroadcastMessage;
import kr.rtustudio.bridge.proxium.api.protocol.internal.PlayerMessage;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyLocation;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.bridge.proxium.api.proxy.request.TeleportRequest;
import kr.rtustudio.bridge.proxium.bukkit.BukkitProxium;
import kr.rtustudio.bridge.redis.Redis;
import kr.rtustudio.bridge.redis.RedisBridge;
import kr.rtustudio.cdi.annotations.Component;
import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.provider.ProviderRegistry;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.nms.NMS;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.framework.bukkit.api.platform.SystemEnvironment;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import kr.rtustudio.framework.bukkit.core.bridge.ProxiumConfig;
import kr.rtustudio.framework.bukkit.core.bridge.RedisConfig;
import kr.rtustudio.framework.bukkit.core.command.ReloadCommand;
import kr.rtustudio.framework.bukkit.core.configuration.CommonTranslation;
import kr.rtustudio.framework.bukkit.core.internal.listeners.InventoryListener;
import kr.rtustudio.framework.bukkit.core.internal.listeners.JoinListener;
import kr.rtustudio.framework.bukkit.core.internal.runnable.CommandLimit;
import kr.rtustudio.framework.bukkit.core.listener.*;
import kr.rtustudio.framework.bukkit.core.module.ModuleFactory;
import kr.rtustudio.framework.bukkit.core.provider.name.VanillaNameProvider;
import kr.rtustudio.framework.bukkit.core.scheduler.Scheduler;
import kr.rtustudio.framework.bukkit.core.storage.StorageManager;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

@Slf4j(topic = "RSFramework")
@Component
public class Framework implements kr.rtustudio.framework.bukkit.api.core.Framework {
    private static final String NMS_PACKAGE_PREFIX = "kr.rtustudio.framework.bukkit.nms.";

    @Getter
    private final net.kyori.adventure.text.Component prefix =
            ComponentFormatter.mini("<gradient:#2979FF:#7C4DFF>RSFramework » </gradient>");

    @Getter private final Map<String, RSPlugin> plugins = new Object2ObjectOpenHashMap<>();
    @Getter private final Map<String, Boolean> hooks = new Object2ObjectOpenHashMap<>();
    @Getter private final ProviderRegistry providerRegistry = new ProviderRegistry();
    @Getter private final BridgeRegistry bridgeRegistry = new BridgeRegistry();
    private final Map<String, StorageManager> storageConfigs = new Object2ObjectOpenHashMap<>();
    @Getter private RSPlugin plugin;
    @Getter private NMS NMS;
    @Getter private String NMSVersion;
    private BukkitProxium proxium;
    @Getter private CommandLimit commandLimit;
    @Getter private CommonTranslation commonTranslation;
    @Getter private ModuleFactory moduleFactory;
    @Getter private kr.rtustudio.framework.bukkit.api.core.scheduler.Scheduler scheduler;
    private final Map<UUID, TeleportRequest> pendingTeleports = new ConcurrentHashMap<>();
    private boolean serverLoaded = false;

    public void loadPlugin(RSPlugin plugin) {
        log.info("loading RSPlugin: {}", plugin.getName());
        plugins.put(plugin.getName(), plugin);
    }

    public void unloadPlugin(RSPlugin plugin) {
        log.info("unloading RSPlugin: {}", plugin.getName());
        plugins.remove(plugin.getName());
        closeStorages(plugin);
    }

    @Override
    public void registerStorage(
            @NotNull RSPlugin plugin, @NotNull String name, @NotNull StorageType type) {
        storageConfigs
                .computeIfAbsent(plugin.getName(), k -> new StorageManager(plugin))
                .registerStorage(name, type);
    }

    @Override
    public Storage getStorage(@NotNull RSPlugin plugin, @NotNull String name) {
        StorageManager impl = storageConfigs.get(plugin.getName());
        return impl != null ? impl.getStorage(name) : null;
    }

    @Override
    public void reloadStorages(@NotNull RSPlugin plugin) {
        StorageManager impl = storageConfigs.get(plugin.getName());
        if (impl != null) impl.reload();
    }

    @Override
    public void closeStorages(@NotNull RSPlugin plugin) {
        StorageManager impl = storageConfigs.remove(plugin.getName());
        if (impl != null) impl.close();
    }

    public boolean isEnabledDependency(String dependency) {
        Boolean hooked = hooks.get(dependency);
        if (hooked != null) return hooked;
        return Bukkit.getPluginManager().isPluginEnabled(dependency);
    }

    public void hookDependency(String dependency) {
        hooks.put(dependency, null);
    }

    public void load(RSPlugin plugin) {
        this.plugin = plugin;
        if (!NBT.preloadApi()) {
            log.warn("NBT-API wasn't initialized properly, disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
        loadNMS(plugin);
        moduleFactory = new ModuleFactory(this);
        providerRegistry.register(NameProvider.class, new VanillaNameProvider());
        scheduler = new Scheduler(plugin);
        loadBridges(plugin);
    }

    private void loadNMS(RSPlugin plugin) {
        NMSVersion = MinecraftVersion.getNMS(MinecraftVersion.getAsText());
        String className =
                NMS_PACKAGE_PREFIX + NMSVersion.toLowerCase() + ".NMS_" + NMSVersion.substring(1);
        try {
            Class<?> clazz = Class.forName(className);
            NMS = (NMS) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn(
                    "Server version is unsupported version ({}), Disabling RSFramework...",
                    NMSVersion);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private void loadBridges(RSPlugin plugin) {
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        ProxiumConfig pc =
                plugin.registerConfiguration(
                        ProxiumConfig.class, ConfigPath.of("Bridge", "Proxium"));
        RedisConfig rc =
                plugin.registerConfiguration(RedisConfig.class, ConfigPath.of("Bridge", "Redis"));

        Proxium existingProxium = bridgeRegistry.get(Proxium.class);
        if (existingProxium == null) {
            proxium = new BukkitProxium(classLoader, plugin.getDataFolder().getPath(), pc);

            proxium.subscribe(
                    BridgeChannel.INTERNAL,
                    BroadcastMessage.class,
                    broadcast -> Notifier.broadcast(broadcast.message()));
            proxium.subscribe(
                    BridgeChannel.INTERNAL,
                    PlayerMessage.class,
                    playerMessage -> {
                        ProxyPlayer target = playerMessage.player();
                        Player player = Bukkit.getPlayer(target.getUniqueId());
                        if (player != null)
                            Notifier.of(plugin, player).send(playerMessage.message());
                    });
            proxium.subscribe(BridgeChannel.INTERNAL, TeleportRequest.class, this::handleTeleport);

            bridgeRegistry.register(Proxium.class, proxium);
        }

        Redis rds = bridgeRegistry.get(Redis.class);
        if (rds == null) {
            bridgeRegistry.register(Redis.class, new RedisBridge(classLoader, rc.toRedisConfig()));
        }
    }

    public void enable(RSPlugin plugin) {
        printStartUp(plugin);
        commonTranslation = new CommonTranslation(plugin);
        registerTeleportListeners(plugin);
        registerInternal(plugin);
    }

    public void disable(RSPlugin plugin) {
        bridgeRegistry.closeAll();
    }

    private void registerInternal(RSPlugin plugin) {
        Bukkit.getPluginManager()
                .registerEvents(
                        new Listener() {
                            @EventHandler
                            public void onLoad(ServerLoadEvent event) {
                                onServerLoad(plugin);
                            }
                        },
                        plugin);
        registerEvent(new ServerLoaded(plugin));
        registerInternalRunnable(plugin);
        registerInternalListener(plugin);
    }

    private void onServerLoad(RSPlugin plugin) {
        serverLoaded = true;
        hooks.replaceAll(
                (name, value) -> {
                    boolean enabled = Bukkit.getPluginManager().isPluginEnabled(name);
                    if (enabled) log.info("{} is hooked!", name);
                    return enabled;
                });
        if (isEnabledDependency("ItemsAdder")) registerEvent(new ItemsAdderLoaded(plugin));
        if (isEnabledDependency("MMOItems")) registerEvent(new MMOItemsLoaded(plugin));
        if (isEnabledDependency("Oraxen")) registerEvent(new OraxenLoaded(plugin));
        if (isEnabledDependency("Nexo")) registerEvent(new NexoLoaded(plugin));
    }

    private void registerInternalRunnable(RSPlugin plugin) {
        commandLimit = new CommandLimit(plugin);
    }

    private void registerInternalListener(RSPlugin plugin) {
        registerEvent(new JoinListener(plugin));
        registerEvent(new InventoryListener(plugin));
    }

    private void printStartUp(RSPlugin plugin) {
        var console = plugin.getAdventure().console();
        console.sendMessage(
                ComponentFormatter.mini(
                        "%s | NMS %s | %s | JDK %s"
                                .formatted(
                                        Bukkit.getName() + "-" + MinecraftVersion.getAsText(),
                                        NMSVersion,
                                        SystemEnvironment.getOS(),
                                        SystemEnvironment.getJDKVersion())));
        for (String line :
                List.of(
                        "╔ <gray>Developed by</gray> ════════════════════════════════════╗",
                        "║ ░█▀▄░░▀█▀░░█░█░░░░░█▀▀░░▀█▀░░█░█░░█▀▄░░▀█▀░░█▀█░ ║",
                        "║ ░█▀▄░░░█░░░█░█░░░░░▀▀█░░░█░░░█░█░░█░█░░░█░░░█░█░ ║",
                        "║ ░▀░▀░░░▀░░░▀▀▀░░░░░▀▀▀░░░▀░░░▀▀▀░░▀▀░░░▀▀▀░░▀▀▀░ ║",
                        "╚══════════════════════════════════════════════════╝")) {
            console.sendMessage(
                    ComponentFormatter.mini("<gradient:#2979FF:#7C4DFF>" + line + "</gradient>"));
        }
    }

    public void registerEvent(RSListener<? extends RSPlugin> listener) {
        Bukkit.getPluginManager().registerEvents(listener, listener.getPlugin());
    }

    public void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload) {
        if (reload) command.registerCommand(new ReloadCommand(command.getPlugin()));
        if (command.getPermission() != null) {
            Bukkit.getPluginManager()
                    .addPermission(
                            new Permission(
                                    command.getPermission(), command.getPermissionDefault()));
        }
        NMS.getCommand().getCommandMap().register(command.getName(), command);
    }

    public void registerPermission(String name, PermissionDefault permissionDefault) {
        Bukkit.getPluginManager().addPermission(new Permission(name, permissionDefault));
    }

    // ── 크로스 서버 텔레포트 ──

    private void handleTeleport(TeleportRequest request) {
        Player player = Bukkit.getPlayer(request.player().getUniqueId());
        if (player == null || !player.isOnline()) {
            pendingTeleports.put(request.player().getUniqueId(), request);
            return;
        }
        executeTeleport(player, request);
    }

    private void executeTeleport(Player player, TeleportRequest request) {
        CraftScheduler.sync(
                player,
                () -> {
                    Location location = null;

                    if (request.targetLocation() != null) {
                        ProxyLocation loc = request.targetLocation();
                        World world = Bukkit.getWorld(loc.world());
                        if (world != null) {
                            location =
                                    new Location(
                                            world,
                                            loc.x(),
                                            loc.y(),
                                            loc.z(),
                                            loc.yaw(),
                                            loc.pitch());
                        }
                    } else if (request.targetPlayer() != null) {
                        Player target = Bukkit.getPlayer(request.targetPlayer().getUniqueId());
                        if (target != null) {
                            location = target.getLocation();
                        }
                    }

                    if (location == null) return;

                    if (MinecraftVersion.isPaper()) {
                        player.teleportAsync(location);
                    } else {
                        player.teleport(location);
                    }
                });
    }

    private void registerTeleportListeners(RSPlugin plugin) {
        Bukkit.getPluginManager()
                .registerEvents(
                        new Listener() {
                            @EventHandler
                            public void onJoin(PlayerJoinEvent event) {
                                TeleportRequest pending =
                                        pendingTeleports.remove(event.getPlayer().getUniqueId());
                                if (pending != null) {
                                    executeTeleport(event.getPlayer(), pending);
                                }
                            }

                            @EventHandler
                            public void onQuit(PlayerQuitEvent event) {
                                pendingTeleports.remove(event.getPlayer().getUniqueId());
                            }
                        },
                        plugin);
    }
}
