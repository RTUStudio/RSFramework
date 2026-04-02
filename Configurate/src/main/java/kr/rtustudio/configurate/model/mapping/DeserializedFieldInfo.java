package kr.rtustudio.configurate.model.mapping;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.AnnotatedType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Record holding deserialized field information and its processor.
 *
 * <p>역직렬화된 필드 정보와 후처리기를 보관하는 레코드.
 *
 * @param <V> field value type
 * @param fieldType annotated type of the field
 * @param deserializedValue deserialized value
 * @param processor field processor (or {@code null} if none)
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
