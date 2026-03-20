package kr.rtustudio.configurate.model.type.number;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.serialize.ScalarSerializer;

import com.google.common.base.Preconditions;

/**
 * 정수 또는 특수 문자열({@code "default"}, {@code "disabled"})을 표현하는 설정 타입.
 *
 * <p>{@link Default}는 값이 없으면 폴백을 사용하고, {@link Disabled}는 기능 비활성화를 나타낸다.
 *
 * <pre>{@code
 * public IntOr.Default maxRetries = IntOr.Default.USE_DEFAULT;
 * // YAML: max-retries: default → maxRetries.or(3) == 3
 * // YAML: max-retries: 5       → maxRetries.or(3) == 5
 *
 * public IntOr.Disabled viewDistance = IntOr.Disabled.DISABLED;
 * // YAML: view-distance: disabled → viewDistance.enabled() == false
 * // YAML: view-distance: 12      → viewDistance.intValue() == 12
 * }</pre>
 */
public interface IntOr {

    Logger LOGGER = LoggerFactory.getLogger(IntOr.class);

    default int or(final int fallback) {
        return this.value().orElse(fallback);
    }

    OptionalInt value();

    default boolean isDefined() {
        return this.value().isPresent();
    }

    default int intValue() {
        return this.value().orElseThrow();
    }

    record Default(OptionalInt value) implements IntOr {
        public static final Default USE_DEFAULT = new Default(OptionalInt.empty());
        private static final String DEFAULT_VALUE = "default";
        public static final ScalarSerializer<Default> SERIALIZER =
                new Serializer<>(Default.class, Default::new, DEFAULT_VALUE, USE_DEFAULT);
    }

    record Disabled(OptionalInt value) implements IntOr {
        public static final Disabled DISABLED = new Disabled(OptionalInt.empty());
        private static final String DISABLED_VALUE = "disabled";
        public static final ScalarSerializer<Disabled> SERIALIZER =
                new Serializer<>(Disabled.class, Disabled::new, DISABLED_VALUE, DISABLED);

        public boolean test(final IntPredicate predicate) {
            return this.value.isPresent() && predicate.test(this.value.getAsInt());
        }

        public boolean enabled() {
            return this.value.isPresent();
        }
    }

    final class Serializer<T extends IntOr> extends OptionalNumSerializer<T, OptionalInt> {

        private Serializer(
                final Class<T> classOfT,
                final Function<OptionalInt, T> factory,
                final String emptySerializedValue,
                final T emptyValue) {
            super(
                    classOfT,
                    emptySerializedValue,
                    emptyValue,
                    OptionalInt::empty,
                    OptionalInt::isEmpty,
                    factory,
                    int.class);
        }

        @Override
        protected OptionalInt full(final String value) {
            return OptionalInt.of(Integer.parseInt(value));
        }

        @Override
        protected OptionalInt full(final Number num) {
            if (num.intValue() != num.doubleValue() || num.intValue() != num.longValue()) {
                LOGGER.error(
                        "{} cannot be converted to an integer without losing information", num);
            }
            return OptionalInt.of(num.intValue());
        }

        @Override
        protected boolean belowZero(final OptionalInt value) {
            Preconditions.checkArgument(value.isPresent());
            return value.getAsInt() < 0;
        }

        @Override
        protected Object serialize(final T item, final Predicate<Class<?>> typeSupported) {
            final OptionalInt value = item.value();
            if (value.isPresent()) {
                return value.getAsInt();
            } else {
                return this.emptySerializedValue;
            }
        }
    }
}
