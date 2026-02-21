package kr.rtustudio.framework.bukkit.api.configuration.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.lang.reflect.AnnotatedType;
import java.util.function.Predicate;

import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

/** Adventure {@link Component}를 MiniMessage 형식으로 직렬화/역직렬화하는 Configurate 직렬화기입니다. */
public class ComponentSerializer extends ScalarSerializer.Annotated<Component> {

    public ComponentSerializer() {
        super(Component.class);
    }

    @Override
    public Component deserialize(final AnnotatedType type, final Object obj)
            throws SerializationException {
        return MiniMessage.miniMessage().deserialize(obj.toString());
    }

    @Override
    protected Object serialize(final Component item, final Predicate<Class<?>> typeSupported) {
        return MiniMessage.miniMessage().serialize(item);
    }
}
