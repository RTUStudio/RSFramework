package kr.rtustudio.framework.bukkit.api.core;

import kr.rtustudio.broker.Broker;
import kr.rtustudio.broker.BrokerRegistry;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.configuration.internal.StorageConfiguration;
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

public interface Framework {

    net.kyori.adventure.text.Component getPrefix();

    RSPlugin getPlugin();

    Map<String, RSPlugin> getPlugins();

    NMS getNMS();

    String getNMSVersion();

    CommandLimit getCommandLimit();

    CommonTranslation getCommonTranslation();

    ModuleFactory getModuleFactory();

    ProviderRegistry getProviderRegistry();

    BrokerRegistry getBrokerRegistry();

    default <T extends Module> T getModule(Class<T> type) {
        return getModuleFactory().getModule(type);
    }

    default <T extends Provider> T getProvider(@NotNull Class<T> type) {
        return getProviderRegistry().get(type);
    }

    default <T extends Provider> void setProvider(@NotNull Class<T> type, @NotNull T provider) {
        getProviderRegistry().register(type, provider);
    }

    default <T extends Broker> T getBroker(@NotNull Class<T> type) {
        return getBrokerRegistry().get(type);
    }

    Scheduler getScheduler();

    void loadPlugin(RSPlugin plugin);

    void unloadPlugin(RSPlugin plugin);

    boolean isEnabledDependency(String dependency);

    void hookDependency(String dependency);

    void load(RSPlugin plugin);

    void enable(RSPlugin plugin);

    void disable(RSPlugin plugin);

    void registerEvent(RSListener<? extends RSPlugin> listener);

    void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload);

    void registerPermission(String name, PermissionDefault permissionDefault);

    /**
     * 주어진 플러그인에 대한 {@link StorageConfiguration} 구현체를 생성한다.
     *
     * <p>Core 모듈에서 {@code StorageConfigurationImpl}을 반환하며, {@link
     * kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration} 생성자에서 호출된다.
     *
     * @param plugin 스토리지 설정을 생성할 플러그인
     * @return 새로운 {@link StorageConfiguration} 인스턴스
     */
    StorageConfiguration createStorageConfiguration(RSPlugin plugin);

    /**
     * 플러그인에 등록된 모든 스토리지를 닫는다.
     *
     * <p>플러그인 비활성화 시 {@code unloadPlugin()}에서 자동 호출된다.
     *
     * @param plugin 스토리지를 닫을 플러그인
     */
    void closeStorages(RSPlugin plugin);
}
