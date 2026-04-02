package kr.rtustudio.configurate.model.constraint;

import lombok.NoArgsConstructor;

import java.lang.annotation.*;
import java.lang.reflect.Type;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Collection of built-in constraints for Configurate object mapping. Provides numeric field
 * constraints such as {@link Min}, {@link Max}, and {@link Positive}.
 *
 * <p>Configurate 객체 매핑에 사용할 수 있는 내장 제약 조건 모음. {@link Min}, {@link Max}, {@link Positive} 등 숫자 필드용
 * 제약을 제공한다.
 */
@NoArgsConstructor
@SuppressWarnings("unused")
public final class Constraints {

    /**
     * Constraint annotation enforcing that the field value is at least the specified minimum.
     *
     * <p>필드 값이 지정한 최소값 이상이어야 함을 나타내는 제약 어노테이션.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Min {
        int value();

        final class Factory implements Constraint.Factory<Min, Number> {
            @Override
            public Constraint<Number> make(Min data, Type type) {
                return value -> {
                    if (value != null && value.intValue() < data.value()) {
                        throw new SerializationException(
                                value + " is less than the min " + data.value());
                    }
                };
            }
        }
    }

    /**
     * Constraint annotation enforcing that the field value is at most the specified maximum.
     *
     * <p>필드 값이 지정한 최대값 이하여야 함을 나타내는 제약 어노테이션.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Max {
        int value();

        final class Factory implements Constraint.Factory<Max, Number> {
            @Override
            public Constraint<Number> make(Max data, Type type) {
                return value -> {
                    if (value != null && value.intValue() > data.value()) {
                        throw new SerializationException(
                                value + " is greater than the max " + data.value());
                    }
                };
            }
        }
    }

    /**
     * Constraint implementation that validates the field value is positive.
     *
     * <p>필드 값이 양수여야 함을 검증하는 제약 구현체.
     */
    public static final class Positive implements Constraint<Number> {
        @Override
        public void validate(@Nullable Number value) throws SerializationException {
            if (value != null && value.doubleValue() <= 0) {
                throw new SerializationException(value + " should be positive");
            }
        }
    }
}
