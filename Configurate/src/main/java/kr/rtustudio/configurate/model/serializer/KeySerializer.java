package kr.rtustudio.configurate.model.serializer;

import net.kyori.adventure.key.Key;

import java.lang.reflect.AnnotatedType;
import java.util.function.Predicate;

import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Configurate serializer for serializing/deserializing Kyori Adventure {@link Key} to and from
 * resource key strings (e.g., {@code "minecraft:block.amethyst_block.place"}).
 *
 * <p>Kyori Adventure {@link Key}를 리소스 키 문자열(예: {@code "minecraft:block.amethyst_block.place"})로
 * 직렬화/역직렬화하는 Configurate 직렬화기. 역직렬화 시 {@code "minecraft:block.amethyst_block.place"} 또는 {@code
 * "block.amethyst_block.place"} 형식의 문자열을 {@link Key}로 변환하며, 네임스페이스가 생략되면 {@code "minecraft"}가 기본값으로
 * 사용된다. 사운드, 아이템 키, 이펙트 키 등 Adventure {@link Key}를 사용하는 모든 설정 필드에 적용된다.
 */
public class KeySerializer extends ScalarSerializer.Annotated<Key> {

    public KeySerializer() {
        super(Key.class);
    }

    @Override
    public Key deserialize(final AnnotatedType type, final Object obj)
            throws SerializationException {
        final String value = obj.toString().toLowerCase();
        try {
            return Key.key(value);
        } catch (Exception e) {
            throw new SerializationException("Invalid resource key: " + value);
        }
    }

    @Override
    protected Object serialize(final Key item, final Predicate<Class<?>> typeSupported) {
        return item.asString();
    }
}
