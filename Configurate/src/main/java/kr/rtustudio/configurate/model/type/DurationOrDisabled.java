package kr.rtustudio.configurate.model.type;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * {@link Duration} 또는 {@code "disabled"} 값을 표현하는 설정 타입.
 *
 * <p>YAML에서 {@code "disabled"} 문자열이면 {@link #USE_DISABLED}로 역직렬화된다. {@link #or(Duration)}으로 폴백 기간을
 * 지정하거나, {@link #value()}로 {@link java.util.Optional}을 직접 확인할 수 있다.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class DurationOrDisabled {

    public static final DurationOrDisabled USE_DISABLED = new DurationOrDisabled(Optional.empty());
    public static final ScalarSerializer<DurationOrDisabled> SERIALIZER = new Serializer();
    private static final String DISABLE_VALUE = "disabled";
    private Optional<Duration> value;

    public DurationOrDisabled(final Optional<Duration> value) {
        this.value = value;
    }

    public Optional<Duration> value() {
        return this.value;
    }

    public void value(final Optional<Duration> value) {
        this.value = value;
    }

    public Duration or(final Duration fallback) {
        return this.value.orElse(fallback);
    }

    private static final class Serializer extends ScalarSerializer<DurationOrDisabled> {
        Serializer() {
            super(DurationOrDisabled.class);
        }

        @Override
        public DurationOrDisabled deserialize(final Type type, final Object obj)
                throws SerializationException {
            if (obj instanceof final String string) {
                if (DISABLE_VALUE.equalsIgnoreCase(string)) {
                    return USE_DISABLED;
                }
                return new DurationOrDisabled(Optional.of(Duration.SERIALIZER.deserialize(string)));
            }
            throw new SerializationException(
                    obj + "(" + type + ") is not a duration or '" + DISABLE_VALUE + "'");
        }

        @Override
        protected Object serialize(
                final DurationOrDisabled item, final Predicate<Class<?>> typeSupported) {
            return item.value.map(Duration::value).orElse(DISABLE_VALUE);
        }
    }
}
