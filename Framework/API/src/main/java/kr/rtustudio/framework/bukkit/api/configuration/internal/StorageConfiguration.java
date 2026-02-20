package kr.rtustudio.framework.bukkit.api.configuration.internal;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Storage name 별로 서로 다른 {@link StorageType}을 지정할 수 있는 스토리지 설정 추상 클래스.
 *
 * <p>설정 파일 {@code Config/Storage.yml}에서 name-type 매핑을 관리하며, 실제 구현은 Core 모듈의 {@code
 * StorageConfigurationImpl}에서 제공한다.
 *
 * <pre>{@code
 * // Storage.yml 예시
 * LocalSQL: "SQLITE"
 * Local: "JSON"
 * }</pre>
 *
 * @see StorageType
 * @see kr.rtustudio.storage.Storage
 */
public abstract class StorageConfiguration extends RSConfiguration.Wrapper<RSPlugin> {

    /**
     * @param plugin 이 설정을 소유하는 플러그인
     */
    public StorageConfiguration(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Storage"));
        setup(this);
    }

    /**
     * 지정한 이름과 타입으로 스토리지를 등록한다.
     *
     * <p>{@code Storage.yml}에 해당 name이 없으면 새로 추가하고, 이미 존재하면 yml에 기록된 타입을 우선 사용한다.
     *
     * @param name 스토리지 식별 이름 (예: {@code "Local"}, {@code "LocalSQL"})
     * @param type 기본 스토리지 타입
     */
    public abstract void registerStorage(@NotNull String name, @NotNull StorageType type);

    /**
     * 지정한 이름으로 스토리지를 등록한다. 기본 타입은 {@link StorageType#JSON}.
     *
     * @param name 스토리지 식별 이름
     */
    public void registerStorage(@NotNull String name) {
        registerStorage(name, StorageType.JSON);
    }

    /**
     * 등록된 스토리지 인스턴스를 반환한다.
     *
     * @param name 스토리지 식별 이름
     * @return 해당 이름의 {@link Storage}, 등록되지 않았으면 {@code null}
     */
    @Nullable
    public abstract Storage getStorage(@NotNull String name);

    /**
     * 현재 등록된 모든 스토리지의 name-type 매핑을 반환한다.
     *
     * @return 수정 불가능한 name → {@link StorageType} 맵
     */
    @NotNull
    public abstract Map<String, StorageType> getStorageMap();
}
