package kr.rtustudio.configurate.model.serializer;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import io.leangen.geantyref.TypeToken;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.EnumLookup;

/**
 * Enum 값을 직렬화/역직렬화하는 Configurate 직렬화기입니다.
 *
 * <p>역직렬화 실패 시 사용 가능한 옵션을 로그에 출력하며, {@code -}를 {@code _}로 치환하여 매칭을 시도합니다.
 */
public class EnumValueSerializer extends ScalarSerializer.Annotated<Enum<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumValueSerializer.class);

    public EnumValueSerializer() {
        super(new TypeToken<Enum<?>>() {});
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public @Nullable Enum<?> deserialize(final AnnotatedType annotatedType, final Object obj)
            throws SerializationException {
        final String enumConstant = obj.toString();
        final Class<? extends Enum> typeClass =
                erase(annotatedType.getType()).asSubclass(Enum.class);
        Enum<?> ret = EnumLookup.lookupEnum(typeClass, enumConstant);
        if (ret == null) {
            ret = EnumLookup.lookupEnum(typeClass, enumConstant.replace("-", "_"));
        }
        if (ret == null) {
            final boolean longer = typeClass.getEnumConstants().length > 10;
            final List<String> options =
                    Arrays.stream(typeClass.getEnumConstants()).limit(10L).map(Enum::name).toList();
            LOGGER.error(
                    "Invalid enum constant provided, expected one of [{}{}], but got {}",
                    String.join(", ", options),
                    longer ? ", ..." : "",
                    enumConstant);
        }
        return ret;
    }

    @Override
    public @NotNull Object serialize(final Enum<?> item, final Predicate<Class<?>> typeSupported) {
        return item.name();
    }
}
