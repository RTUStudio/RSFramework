package kr.rtustudio.framework.bukkit.api.configuration.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

public interface FieldProcessor<V> {

    @Nullable
    V process(final AnnotatedType target, @Nullable V deserializedValue, @Nullable V valueInField)
            throws SerializationException;

    interface Factory<A extends Annotation, T> {

        FieldProcessor<T> make(A data, AnnotatedType annotatedType);
    }
}
