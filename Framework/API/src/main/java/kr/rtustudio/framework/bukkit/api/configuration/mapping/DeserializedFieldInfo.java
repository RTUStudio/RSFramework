package kr.rtustudio.framework.bukkit.api.configuration.mapping;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.AnnotatedType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 역직렬화된 필드 정보와 후처리기를 보관하는 레코드입니다.
 *
 * @param <V> 필드 값 타입
 * @param fieldType 필드의 어노테이션 타입
 * @param deserializedValue 역직렬화된 값
 * @param processor 필드 후처리기 (없으면 {@code null})
 */
public record DeserializedFieldInfo<V>(
        AnnotatedType fieldType, Object deserializedValue, @Nullable FieldProcessor<V> processor) {

    @SuppressWarnings("unchecked")
    public @Nullable V runProcessor(
            final @Nullable Object valueInField, final @Nullable Object deserializedValue)
            throws SerializationException {
        checkState(this.processor != null, "processor is null");
        return this.processor.process(this.fieldType, (V) deserializedValue, (V) valueInField);
    }
}
