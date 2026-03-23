package kr.rtustudio.framework.bukkit.api.core;

import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeRegistry;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.configuration.CommonTranslation;
import kr.rtustudio.framework.bukkit.api.core.internal.runnable.CommandLimit;
import kr.rtustudio.framework.bukkit.api.core.module.Module;
import kr.rtustudio.framework.bukkit.api.core.module.ModuleFactory;
import kr.rtustudio.framework.bukkit.api.core.provider.Provider;
import kr.rtustudio.framework.bukkit.api.core.provider.ProviderRegistry;
import kr.rtustudio.framework.bukkit.api.core.scheduler.Scheduler;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.nms.NMS;

import java.util.Map;

import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

/**
 * Centrally manages modules, providers, storage, commands, and other global state.
 *
 * <p>RSFramework의 핵심 관리 기능을 정의하는 인터페이스. 모듈, 프로바이더, 스토리지, 명령어 등의 전역 상태를 통합 관리한다.
 */
public interface Framework {

    /** Returns the global framework prefix component. / 프레임워크 전역 접두사 컴포넌트를 반환한다. */
    net.kyori.adventure.text.Component getPrefix();

    /** Returns the main framework plugin instance. / 프레임워크 메인 플러그인 인스턴스를 반환한다. */
    RSPlugin getPlugin();

    /**
     * Returns all plugins loaded by the framework. Key is the plugin name. / 프레임워크에 로드된 모든 플러그인 맵을
     * 반환한다.
     */
    Map<String, RSPlugin> getPlugins();

    /** Returns the NMS access interface. / NMS 접근 인터페이스를 반환한다. */
    NMS getNMS();

    /** Returns the NMS version string (e.g. {@code "v1_21_R5"}). / 현재 서버의 NMS 버전 문자열을 반환한다. */
    String getNMSVersion();

    /** Returns the command execution cooldown manager. / 명령어 실행 쿨다운 관리자를 반환한다. */
    CommandLimit getCommandLimit();

    /** Returns the common translation manager. / 프레임워크 공통 번역 관리자를 반환한다. */
    CommonTranslation getCommonTranslation();

    /** Returns the module factory. / 모듈 팩토리를 반환한다. */
    ModuleFactory getModuleFactory();

    /** Returns the provider registry. / 프로바이더 레지스트리를 반환한다. */
    ProviderRegistry getProviderRegistry();

    /** Returns the bridge registry. / 브릿지 레지스트리를 반환한다. */
    BridgeRegistry getBridgeRegistry();

    /**
     * Retrieves a module by type.
     *
     * <p>지정한 타입의 모듈을 조회한다.
     *
     * @param type module interface class
     * @param <T> module type
     * @return registered module instance
     */
    default <T extends Module> T getModule(Class<T> type) {
        return getModuleFactory().getModule(type);
    }

    /**
     * Retrieves a provider by type.
     *
     * <p>지정한 타입의 프로바이더를 조회한다.
     *
     * @param type provider interface class
     * @param <T> provider type
     * @return registered provider instance, or {@code null}
     */
    default <T extends Provider> T getProvider(@NotNull Class<T> type) {
        return getProviderRegistry().get(type);
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
    default <T extends Provider> void setProvider(@NotNull Class<T> type, @NotNull T provider) {
        getProviderRegistry().register(type, provider);
    }

    /**
     * Retrieves a bridge by type.
     *
     * <p>지정한 타입의 브릿지를 조회한다.
     *
     * @param type bridge interface class
     * @param <T> bridge type
     * @return registered bridge instance
     */
    default <T extends Bridge> T getBridge(@NotNull Class<T> type) {
        return getBridgeRegistry().get(type);
    }

    /**
     * Returns the platform-specific scheduler (Spigot/Paper/Folia compatible). / 플랫폼별 스케줄러를 반환한다.
     */
    Scheduler getScheduler();

    /**
     * Loads a plugin into the framework.
     *
     * <p>플러그인을 프레임워크에 로드한다.
     *
     * @param plugin plugin to load
     */
    void loadPlugin(RSPlugin plugin);

    /**
     * Unloads a plugin from the framework.
     *
     * <p>플러그인을 프레임워크에서 언로드한다.
     *
     * @param plugin plugin to unload
     */
    void unloadPlugin(RSPlugin plugin);

    /**
     * Checks whether the specified dependency plugin is enabled.
     *
     * <p>지정한 의존 플러그인이 활성화되어 있는지 확인한다.
     *
     * @param dependency dependency plugin name
     * @return whether enabled
     */
    boolean isEnabledDependency(String dependency);

    /**
     * Hooks a soft-dependency plugin.
     *
     * <p>소프트 의존 플러그인을 후킹한다.
     *
     * @param dependency dependency plugin name
     */
    void hookDependency(String dependency);

    /**
     * Executes the plugin's load phase.
     *
     * <p>플러그인의 로드 단계를 실행한다.
     *
     * @param plugin target plugin
     */
    void load(RSPlugin plugin);

    /**
     * Executes the plugin's enable phase.
     *
     * <p>플러그인의 활성화 단계를 실행한다.
     *
     * @param plugin target plugin
     */
    void enable(RSPlugin plugin);

    /**
     * Executes the plugin's disable phase.
     *
     * <p>플러그인의 비활성화 단계를 실행한다.
     *
     * @param plugin target plugin
     */
    void disable(RSPlugin plugin);

    /**
     * Registers an event listener with the framework.
     *
     * <p>이벤트 리스너를 프레임워크에 등록한다.
     *
     * @param listener listener to register
     */
    void registerEvent(RSListener<? extends RSPlugin> listener);

    /**
     * Registers a command with the framework.
     *
     * <p>명령어를 프레임워크에 등록한다.
     *
     * @param command command to register
     * @param reload if {@code true}, immediately refreshes the server command map
     */
    void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload);

    /**
     * Registers a permission node on the server.
     *
     * <p>권한 노드를 서버에 등록한다.
     *
     * @param name full permission node name (lowercase)
     * @param permissionDefault default permission level
     */
    void registerPermission(String name, PermissionDefault permissionDefault);

    /**
     * Registers a storage with the specified name and type.
     *
     * <p>지정한 이름과 타입으로 스토리지를 등록한다.
     *
     * @param plugin owning plugin
     * @param name storage identifier
     * @param type default storage type
     */
    void registerStorage(
            @NotNull RSPlugin plugin,
            @NotNull String name,
            @NotNull kr.rtustudio.storage.StorageType type);

    /**
     * Returns a registered storage instance.
     *
     * <p>등록된 스토리지 인스턴스를 반환한다.
     *
     * @param plugin owning plugin
     * @param name storage identifier
     * @return the {@link kr.rtustudio.storage.Storage}, or {@code null} if not registered
     */
    kr.rtustudio.storage.Storage getStorage(@NotNull RSPlugin plugin, @NotNull String name);

    /**
     * Reloads storage configuration for the plugin.
     *
     * <p>플러그인의 스토리지 설정을 리로드한다.
     *
     * @param plugin plugin to reload
     */
    void reloadStorages(@NotNull RSPlugin plugin);

    /**
     * Closes all storage instances registered for the plugin.
     *
     * <p>플러그인에 등록된 모든 스토리지를 닫는다.
     *
     * @param plugin the plugin
     */
    void closeStorages(@NotNull RSPlugin plugin);
}
