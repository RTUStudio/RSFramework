package kr.rtustudio.framework.bukkit.api.configuration.mapping;

import static com.google.common.base.Preconditions.checkState;

import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.configuration.serializer.collection.map.MapSerializer;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * {@link ConfigurationPart} 내부 맵 필드에 사용하여, 기본 키가 사용자에 의해 제거되지 않도록 보호하는 어노테이션입니다.
 *
 * <p>설정 리로드 시 맵이 다시 병합되므로, 키가 무한히 누적되지 않도록 주의해야 합니다.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MergeMap {

    Logger LOGGER = LoggerFactory.getLogger(MergeMap.class);
    Definition<MergeMap, Map<?, ?>, Factory> DEFINITION =
            new Definition<>(MergeMap.class, MapSerializer.TYPE, new Factory());

    /**
     * {@code true}이면 역직렬화 시 기존 키 외의 새 키 추가를 허용하지 않는다.
     *
     * @return 제한 여부
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
