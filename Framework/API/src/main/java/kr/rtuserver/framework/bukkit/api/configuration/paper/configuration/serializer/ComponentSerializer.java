package kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.AnnotatedType;
import java.util.function.Predicate;

public class ComponentSerializer extends ScalarSerializer.Annotated<Component> {

    public ComponentSerializer() {
        super(Component.class);
    }

    @Override
    public Component deserialize(final AnnotatedType type, final Object obj) throws SerializationException {
        return MiniMessage.miniMessage().deserialize(obj.toString());
    }

    @Override
    protected Object serialize(final Component item, final Predicate<Class<?>> typeSupported) {
        return MiniMessage.miniMessage().serialize(item);
    }
}
