package kr.rtustudio.framework.bukkit.api.configuration.constraint;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * Configurate 객체 매핑 시 필드에 적용할 커스텀 제약 조건을 지정하는 어노테이션입니다.
 *
 * <p>{@code value}에 {@link org.spongepowered.configurate.objectmapping.meta.Constraint} 구현체 클래스를
 * 지정합니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
public @interface Constraint {

    Class<? extends org.spongepowered.configurate.objectmapping.meta.Constraint<?>> value();

    class Factory
            implements org.spongepowered.configurate.objectmapping.meta.Constraint.Factory<
                    Constraint, Object> {
        @SuppressWarnings("unchecked")
        @Override
        public org.spongepowered.configurate.objectmapping.meta.Constraint<Object> make(
                final Constraint data, final Type type) {
            try {
                final Constructor<
                                ? extends
                                        org.spongepowered.configurate.objectmapping.meta.Constraint<
                                                ?>>
                        constructor = data.value().getDeclaredConstructor();
                constructor.trySetAccessible();
                return (org.spongepowered.configurate.objectmapping.meta.Constraint<Object>)
                        constructor.newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Could not create constraint", e);
            }
        }
    }
}
