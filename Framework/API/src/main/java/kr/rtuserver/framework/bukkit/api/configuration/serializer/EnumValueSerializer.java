package kr.rtuserver.framework.bukkit.api.configuration.serializer;

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

/** Enum serializer that lists options if fails and accepts `-` as `_`. */
public class EnumValueSerializer extends ScalarSerializer.Annotated<Enum<?>> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EnumValueSerializer.class); // TODO: 이거 맞냐...

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
