package kr.rtustudio.framework.bukkit.api.configuration;

import kr.rtustudio.framework.bukkit.api.configuration.constraint.Constraint;
import kr.rtustudio.framework.bukkit.api.configuration.constraint.Constraints;
import kr.rtustudio.framework.bukkit.api.configuration.serializer.ComponentSerializer;
import kr.rtustudio.framework.bukkit.api.configuration.serializer.EnumValueSerializer;
import kr.rtustudio.framework.bukkit.api.configuration.serializer.collection.map.FlattenedMapSerializer;
import kr.rtustudio.framework.bukkit.api.configuration.serializer.collection.map.MapSerializer;
import kr.rtustudio.framework.bukkit.api.configuration.type.BooleanOrDefault;
import kr.rtustudio.framework.bukkit.api.configuration.type.Duration;
import kr.rtustudio.framework.bukkit.api.configuration.type.DurationOrDisabled;
import kr.rtustudio.framework.bukkit.api.configuration.type.number.DoubleOr;
import kr.rtustudio.framework.bukkit.api.configuration.type.number.IntOr;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationSerializer {

    public static ConfigurationOptions apply(ConfigurationOptions options) {
        return options.serializers(ConfigurationSerializer::registerSerializers)
                .mapFactory(MapFactories.insertionOrdered());
    }

    /** Apply configuration options with the built-in serializers plus any provided extras. */
    public static ConfigurationOptions apply(
            ConfigurationOptions options, Consumer<TypeSerializerCollection.Builder> extra) {
        return options.serializers(
                        builder -> {
                            if (extra != null) extra.accept(builder);
                            registerSerializers(builder);
                        })
                .mapFactory(MapFactories.insertionOrdered());
    }

    public static ObjectMapper.Factory.Builder apply(ObjectMapper.Factory.Builder builder) {
        return builder.addConstraint(Constraint.class, new Constraint.Factory())
                .addConstraint(Constraints.Min.class, Number.class, new Constraints.Min.Factory())
                .addConstraint(Constraints.Max.class, Number.class, new Constraints.Max.Factory());
    }

    private static void registerSerializers(TypeSerializerCollection.Builder builder) {
        builder.register(FlattenedMapSerializer.TYPE, new FlattenedMapSerializer(false))
                .register(MapSerializer.TYPE, new MapSerializer(false))
                .register(new EnumValueSerializer())
                .register(new ComponentSerializer())
                .register(IntOr.Default.SERIALIZER)
                .register(IntOr.Disabled.SERIALIZER)
                .register(DoubleOr.Default.SERIALIZER)
                .register(DoubleOr.Disabled.SERIALIZER)
                .register(BooleanOrDefault.SERIALIZER)
                .register(Duration.SERIALIZER)
                .register(DurationOrDisabled.SERIALIZER);
    }
}
