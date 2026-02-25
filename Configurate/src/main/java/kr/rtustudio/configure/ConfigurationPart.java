package kr.rtustudio.configure;

import java.util.function.Consumer;

/**
 * 설정 파일의 개별 섹션을 나타내는 마커 추상 클래스입니다.
 *
 * <p>Configurate의 객체 매핑 대상이 되며, {@link RSConfiguration#registerConfiguration}으로 등록합니다.
 */
public abstract class ConfigurationPart {

    /**
     * 객체를 생성한 뒤 초기화 로직을 적용하여 반환하는 헬퍼 메서드.
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
