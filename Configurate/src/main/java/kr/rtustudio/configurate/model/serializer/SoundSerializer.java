package kr.rtustudio.configurate.model.serializer;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.lang.reflect.AnnotatedType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

/**
 * Configurate serializer for serializing/deserializing Kyori Adventure {@link Sound}.
 *
 * <p>Kyori Adventure {@link Sound}를 직렬화/역직렬화하는 Configurate 직렬화기.
 */
public class SoundSerializer implements TypeSerializer.Annotated<Sound> {

    @Override
    public Sound deserialize(final AnnotatedType type, final ConfigurationNode node)
            throws SerializationException {
        if (node.isMap()) {
            final Key key = node.node("key").get(Key.class);
            if (key == null) {
                throw new SerializationException("Sound key must be provided");
            }

            final Sound.Source source =
                    node.node("source").get(Sound.Source.class, Sound.Source.MASTER);
            final float volume = node.node("volume").getFloat(1.0f);
            final float pitch = node.node("pitch").getFloat(1.0f);

            Sound.Builder builder =
                    Sound.sound().type(key).source(source).volume(volume).pitch(pitch);

            return builder.build();
        } else if (!node.virtual()) {
            final Key key = node.get(Key.class);
            if (key != null) {
                return Sound.sound(key, Sound.Source.MASTER, 1.0f, 1.0f);
            }
        }
        return null;
    }

    @Override
    public void serialize(
            final AnnotatedType type, final @Nullable Sound obj, final ConfigurationNode node)
            throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        if (obj.source() == Sound.Source.MASTER && obj.volume() == 1.0f && obj.pitch() == 1.0f) {
            node.set(Key.class, obj.name());
        } else {
            node.node("key").set(Key.class, obj.name());

            if (obj.source() != Sound.Source.MASTER) {
                node.node("source").set(Sound.Source.class, obj.source());
            } else {
                node.node("source").set(null);
            }

            if (obj.volume() != 1.0f) {
                node.node("volume").set(obj.volume());
            } else {
                node.node("volume").set(null);
            }

            if (obj.pitch() != 1.0f) {
                node.node("pitch").set(obj.pitch());
            } else {
                node.node("pitch").set(null);
            }
        }
    }
}
