package kr.rtustudio.bridge.proxium.core.config;

import io.leangen.geantyref.GenericTypeReflector;
import kr.rtustudio.configurate.model.Configuration;
import kr.rtustudio.configurate.model.ConfigurationPart;

import java.lang.reflect.Type;
import java.nio.file.Path;

import org.spongepowered.configurate.ConfigurateException;

/**
 * 플랫폼 독립적인 YAML 설정 로더.
 *
 * <p>Bukkit 등 특정 플랫폼에 의존하지 않고 {@link ConfigurationPart} 기반 설정을 로드·리로드한다.
 * Proxium처럼 플랫폼 API가 없는 모듈에서 사용한다.
 *
 * <pre>{@code
 * SimpleConfiguration<MySettings> config =
 *     new SimpleConfiguration<>(MySettings.class, dataFolder.resolve("config.yml"));
 * MySettings settings = config.load();
 * config.reload(settings);
 * }</pre>
 *
 * @param <T> {@link ConfigurationPart}를 상속하는 설정 타입
 */
public class SimpleConfiguration<T extends ConfigurationPart> extends Configuration<T> {

    private final int version;

    public SimpleConfiguration(Class<T> type, Path path) {
        this(type, path, 0);
    }

    public SimpleConfiguration(Class<T> type, Path path, int version) {
        super(type, path, null);
        this.version = version;
    }

    @Override
    protected int configVersion() {
        return version;
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(GenericTypeReflector.erase(type));
    }

    /**
     * 설정 파일을 로드하여 새 인스턴스를 생성한다.
     *
     * @return 로드된 설정 인스턴스
     */
    public T load() throws ConfigurateException {
        return initializeConfiguration(creator(this.type, true));
    }

    /**
     * 기존 인스턴스에 파일 내용을 다시 로드한다.
     *
     * @param instance 리로드할 기존 인스턴스
     */
    public void reload(T instance) throws ConfigurateException {
        initializeConfiguration(reloader(this.type, instance));
    }
}
