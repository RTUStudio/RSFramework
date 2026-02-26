package kr.rtustudio.configurate.model.type;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * {@code "30s"}, {@code "5m"}, {@code "1h"}, {@code "2d"} 형식의 시간 기간을 표현하는 설정 타입.
 *
 * <p>지원하는 단위: {@code s}(초), {@code m}(분), {@code h}(시), {@code d}(일). 소수점도 허용한다 (예: {@code "1.5h"}
 * = 5400초).
 *
 * <pre>{@code
 * public Duration timeout = Duration.of("30s");
 * long seconds = timeout.seconds(); // 30
 * long ticks   = timeout.ticks();   // 600
 * }</pre>
 */
public final class Duration {

    public static final ScalarSerializer<Duration> SERIALIZER = new Serializer();
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern NOT_NUMERIC = Pattern.compile("[^-\\d.]");
    private final long seconds;
    private final String value;

    private Duration(String value) {
        this.value = value;
        this.seconds = getSeconds(value);
    }

    public static Duration of(String time) {
        return new Duration(time);
    }

    private static int getSeconds(String str) {
        str = SPACE.matcher(str).replaceAll("");
        final char unit = str.charAt(str.length() - 1);
        str = NOT_NUMERIC.matcher(str).replaceAll("");
        double num;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            num = 0D;
        }
        switch (unit) {
            case 'd':
                num *= (double) 60 * 60 * 24;
                break;
            case 'h':
                num *= (double) 60 * 60;
                break;
            case 'm':
                num *= 60;
                break;
            default:
            case 's':
                break;
        }
        return (int) num;
    }

    public long seconds() {
        return this.seconds;
    }

    public long ticks() {
        return this.seconds * 20;
    }

    public String value() {
        return this.value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Duration duration = (Duration) o;
        return seconds == duration.seconds && this.value.equals(duration.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.seconds, this.value);
    }

    @Override
    public String toString() {
        return "Duration{" + "seconds=" + this.seconds + ", value='" + this.value + '\'' + '}';
    }

    static final class Serializer extends ScalarSerializer<Duration> {
        private Serializer() {
            super(Duration.class);
        }

        @Override
        public Duration deserialize(Type type, Object obj) throws SerializationException {
            return new Duration(obj.toString());
        }

        @Override
        protected Object serialize(Duration item, Predicate<Class<?>> typeSupported) {
            return item.value();
        }
    }
}
