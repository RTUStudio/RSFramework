package kr.rtuserver.framework.bukkit.api.configuration.mapping;

import static com.google.common.base.Preconditions.checkState;

import kr.rtuserver.framework.bukkit.api.configuration.ConfigurationPart;
import kr.rtuserver.framework.bukkit.api.configuration.serializer.collection.map.MapSerializer;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * For use in maps inside {@link ConfigurationPart}s that have default keys that shouldn't be
 * removed by users
 *
 * <p>Note that when the config is reloaded, the maps will be merged again, so make sure this map
 * can't accumulate keys overtime.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MergeMap {

    Logger LOGGER = LoggerFactory.getLogger("MergeMap"); // TODO: 이거 맞냐...
    Definition<MergeMap, Map<?, ?>, Factory> DEFINITION =
            new Definition<>(MergeMap.class, MapSerializer.TYPE, new Factory());

    /**
     * If marked as restricted, the field won't allow new keys beyond what is already in the field
     * when deserializing.
     *
     * @return True if restricted
     */
    boolean restricted() default true;

    final class Factory implements FieldProcessor.Factory<MergeMap, Map<?, ?>> {

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public FieldProcessor<Map<?, ?>> make(
                final MergeMap data, final AnnotatedType annotatedType) {
            return (target, deserializedValue, valueInField) -> {
                if (valueInField != null && deserializedValue != null) {
                    if (data.restricted()) {
                        final Set<?> invalidKeys =
                                Sets.difference(deserializedValue.keySet(), valueInField.keySet())
                                        .immutableCopy();
                        for (final Object invalidKey : invalidKeys) {
                            LOGGER.error(
                                    "The key {} is not allowed to be added to the field {}",
                                    invalidKey,
                                    target);
                        }
                        ((Map) deserializedValue).keySet().removeAll(invalidKeys);
                    }
                    ((Map) valueInField).putAll(deserializedValue);
                    return valueInField;
                } else {
                    checkState(
                            !data.restricted() || valueInField != null,
                            "If marked as restricted, field must have a value");
                    return deserializedValue;
                }
            };
        }
    }
}
