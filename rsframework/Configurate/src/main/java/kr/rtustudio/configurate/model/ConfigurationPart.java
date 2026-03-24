package kr.rtustudio.configurate.model;

import java.util.function.Consumer;

/**
 * YAML 설정 파일의 개별 섹션을 나타내는 베이스 클래스.
 *
 * <p>{@code @ConfigSerializable}과 함께 사용하여 Configurate 객체 매핑 대상으로 등록한다. 비정적 내부 클래스로 선언하면 {@link
 * kr.rtustudio.configurate.model.mapping.InnerClassFieldDiscoverer}가 자동으로 인스턴스를 생성한다.
 *
 * <pre>{@code
 * @ConfigSerializable
 * public class StorageSettings extends ConfigurationPart {
 *     public String type = "json";
 *     public int poolSize = 10;
 * }
 * }</pre>
 */
public abstract class ConfigurationPart {

    /**
     * 객체를 생성한 뒤 초기화 로직을 적용하여 반환한다.
     *
     * @param object 초기화할 객체
     * @param consumer 초기화 로직
     * @param <T> 객체 타입
     * @return 초기화된 객체
     */
    public <T> T make(T object, Consumer<? super T> consumer) {
        consumer.accept(object);
        return object;
    }
}
