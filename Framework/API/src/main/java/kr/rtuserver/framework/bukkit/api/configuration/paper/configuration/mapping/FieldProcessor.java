package kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.mapping;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;

public interface FieldProcessor<V> {

    @Nullable V process(final AnnotatedType target, @Nullable V deserializedValue, @Nullable V valueInField) throws SerializationException;

    interface Factory<A extends Annotation, T> {

        FieldProcessor<T> make(A data, AnnotatedType annotatedType);
    }
}
