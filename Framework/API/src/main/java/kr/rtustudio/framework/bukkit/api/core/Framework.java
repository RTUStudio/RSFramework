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
 * RSFramework의 핵심 관리 기능을 정의하는 인터페이스입니다.
 *
 * <p>모듈, 프로바이더, 스토리지, 명령어 등의 전역 상태를 통합 관리합니다.
 */
public interface Framework {

    /** 프레임워크 전역 접두사 컴포넌트를 반환한다. */
    net.kyori.adventure.text.Component getPrefix();

    /** 프레임워크 메인 플러그인 인스턴스를 반환한다. */
    RSPlugin getPlugin();

    /** 프레임워크에 로드된 모든 플러그인 맵을 반환한다. 키는 플러그인 이름이다. */
    Map<String, RSPlugin> getPlugins();

    /** NMS(Net Minecraft Server) 접근 인터페이스를 반환한다. */
    NMS getNMS();

    /** 현재 서버의 NMS 버전 문자열을 반환한다 (예: {@code "v1_21_R5"}). */
    String getNMSVersion();

    /** 명령어 실행 쿨다운 관리자를 반환한다. */
    CommandLimit getCommandLimit();

    /** 프레임워크 공통 번역 관리자를 반환한다. */
    CommonTranslation getCommonTranslation();

    /** 모듈 팩토리를 반환한다. */
    ModuleFactory getModuleFactory();

    /** 프로바이더 레지스트리를 반환한다. */
    ProviderRegistry getProviderRegistry();

    /** 브릿지 레지스트리를 반환한다. */
    BridgeRegistry getBridgeRegistry();

    /**
     * 지정한 타입의 모듈을 조회한다.
     *
     * @param type 모듈 인터페이스 클래스
     * @param <T> 모듈 타입
     * @return 등록된 모듈 인스턴스
     */
    default <T extends Module> T getModule(Class<T> type) {
        return getModuleFactory().getModule(type);
    }

    /**
     * 지정한 타입의 프로바이더를 조회한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @param <T> 프로바이더 타입
     * @return 등록된 프로바이더 인스턴스, 없으면 {@code null}
     */
    default <T extends Provider> T getProvider(@NotNull Class<T> type) {
        return getProviderRegistry().get(type);
    }

    /**
     * 프로바이더 인스턴스를 등록하거나 교체한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @param provider 등록할 프로바이더 인스턴스
     * @param <T> 프로바이더 타입
     */
    default <T extends Provider> void setProvider(@NotNull Class<T> type, @NotNull T provider) {
        getProviderRegistry().register(type, provider);
    }

    /**
     * 지정한 타입의 브릿지를 조회한다.
     *
     * @param type 브릿지 인터페이스 클래스
     * @param <T> 브릿지 타입
     * @return 등록된 브릿지 인스턴스
     */
    default <T extends Bridge> T getBridge(@NotNull Class<T> type) {
        return getBridgeRegistry().get(type);
    }

    /** 플랫폼별 스케줄러를 반환한다 (Spigot/Paper/Folia 호환). */
    Scheduler getScheduler();

    /**
     * 플러그인을 프레임워크에 로드한다.
     *
     * @param plugin 로드할 플러그인
     */
    void loadPlugin(RSPlugin plugin);

    /**
     * 플러그인을 프레임워크에서 언로드한다.
     *
     * @param plugin 언로드할 플러그인
     */
    void unloadPlugin(RSPlugin plugin);

    /**
     * 지정한 의존 플러그인이 활성화되어 있는지 확인한다.
     *
     * @param dependency 의존 플러그인 이름
     * @return 활성화 여부
     */
    boolean isEnabledDependency(String dependency);

    /**
     * 소프트 의존 플러그인을 후킹한다.
     *
     * @param dependency 의존 플러그인 이름
     */
    void hookDependency(String dependency);

    /**
     * 플러그인의 로드 단계를 실행한다.
     *
     * @param plugin 대상 플러그인
     */
    void load(RSPlugin plugin);

    /**
     * 플러그인의 활성화 단계를 실행한다.
     *
     * @param plugin 대상 플러그인
     */
    void enable(RSPlugin plugin);

    /**
     * 플러그인의 비활성화 단계를 실행한다.
     *
     * @param plugin 대상 플러그인
     */
    void disable(RSPlugin plugin);

    /**
     * 이벤트 리스너를 프레임워크에 등록한다.
     *
     * @param listener 등록할 리스너
     */
    void registerEvent(RSListener<? extends RSPlugin> listener);

    /**
     * 명령어를 프레임워크에 등록한다.
     *
     * @param command 등록할 명령어
     * @param reload {@code true}이면 서버 명령어 맵을 즉시 갱신한다
     */
    void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload);

    /**
     * 권한 노드를 서버에 등록한다.
     *
     * @param name 권한 노드 전체 이름 (소문자)
     * @param permissionDefault 기본 권한 수준
     */
    void registerPermission(String name, PermissionDefault permissionDefault);

    /**
     * 지정한 이름과 타입으로 스토리지를 등록한다.
     *
     * @param plugin 스토리지를 등록할 플러그인
     * @param name 스토리지 식별 이름
     * @param type 기본 스토리지 타입
     */
    void registerStorage(
            @NotNull RSPlugin plugin,
            @NotNull String name,
            @NotNull kr.rtustudio.storage.StorageType type);

    /**
     * 등록된 스토리지 인스턴스를 반환한다.
     *
     * @param plugin 스토리지를 소유하는 플러그인
     * @param name 스토리지 식별 이름
     * @return 해당 이름의 {@link kr.rtustudio.storage.Storage}, 등록되지 않았으면 {@code null}
     */
    kr.rtustudio.storage.Storage getStorage(@NotNull RSPlugin plugin, @NotNull String name);

    /**
     * 플러그인의 스토리지 설정을 리로드한다.
     *
     * @param plugin 리로드할 플러그인
     */
    void reloadStorages(@NotNull RSPlugin plugin);

    /**
     * 플러그인에 등록된 모든 스토리지를 닫는다.
     *
     * @param plugin 스토리지를 닫을 플러그인
     */
    void closeStorages(@NotNull RSPlugin plugin);
}
