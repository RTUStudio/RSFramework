package kr.rtustudio.configure.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Configurate 객체 매핑 시 필드 값을 후처리하는 인터페이스입니다.
 *
 * <p>역직렬화된 값과 기존 필드 값을 비교·병합하는 등의 로직을 적용합니다.
 *
 * @param <V> 필드 값 타입
 */
public interface FieldProcessor<V> {

    @Nullable
    V process(final AnnotatedType target, @Nullable V deserializedValue, @Nullable V valueInField)
            throws SerializationException;

    interface Factory<A extends Annotation, T> {

        FieldProcessor<T> make(A data, AnnotatedType annotatedType);
    }
}
