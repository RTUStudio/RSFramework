package kr.rtustudio.configurate.model.constraint;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * 필드에 커스텀 제약 조건을 적용하는 어노테이션.
 *
 * <p>{@code value}에 {@link org.spongepowered.configurate.objectmapping.meta.Constraint} 구현체를 지정하면,
 * 역직렬화 시 해당 제약이 자동으로 검증된다.
 *
 * <pre>{@code
 * @Constraint(Constraints.Positive.class)
 * public int maxPlayers = 20;
 * }</pre>
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
