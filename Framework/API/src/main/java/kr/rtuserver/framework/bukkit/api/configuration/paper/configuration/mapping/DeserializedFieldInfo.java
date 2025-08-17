package kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.mapping;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.AnnotatedType;

import static com.google.common.base.Preconditions.checkState;

public record DeserializedFieldInfo<V>(AnnotatedType fieldType, Object deserializedValue, @Nullable FieldProcessor<V> processor) {

    @SuppressWarnings("unchecked")
    public @Nullable V runProcessor(final @Nullable Object valueInField, final @Nullable Object deserializedValue) throws SerializationException {
        checkState(this.processor != null, "processor is null");
        return this.processor.process(this.fieldType, (V) deserializedValue, (V) valueInField);
    }
}
