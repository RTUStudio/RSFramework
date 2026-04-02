package kr.rtustudio.configurate.model;

import kr.rtustudio.configurate.model.constraint.Constraint;
import kr.rtustudio.configurate.model.constraint.Constraints;
import kr.rtustudio.configurate.model.serializer.ComponentSerializer;
import kr.rtustudio.configurate.model.serializer.EnumValueSerializer;
import kr.rtustudio.configurate.model.serializer.KeySerializer;
import kr.rtustudio.configurate.model.serializer.SoundSerializer;
import kr.rtustudio.configurate.model.serializer.collection.map.FlattenedMapSerializer;
import kr.rtustudio.configurate.model.serializer.collection.map.MapSerializer;
import kr.rtustudio.configurate.model.type.BooleanOrDefault;
import kr.rtustudio.configurate.model.type.Duration;
import kr.rtustudio.configurate.model.type.DurationOrDisabled;
import kr.rtustudio.configurate.model.type.number.DoubleOr;
import kr.rtustudio.configurate.model.type.number.IntOr;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.sound.Sound;

import java.util.function.Consumer;

import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;

/**
 * Utility that batch-registers built-in serializers and constraints to {@link ConfigurationOptions}
 * and {@link ObjectMapper.Factory.Builder}.
 *
 * <p>{@link ConfigurationOptions}와 {@link ObjectMapper.Factory.Builder}에 내장 직렬화기 및 제약 조건을 일괄 등록하는
 * 유틸리티.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationSerializer {

    public static ConfigurationOptions apply(ConfigurationOptions options) {
        return options.serializers(ConfigurationSerializer::registerSerializers)
                .mapFactory(MapFactories.insertionOrdered());
    }

    /**
     * Applies both built-in and additional serializers.
     *
     * <p>내장 직렬화와 추가 직렬화를 함께 적용한다.
     *
     * @param options base options
     * @param extra additional type serializers
     * @return options with serializers applied
     */
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
                .register(new KeySerializer())
                .register(Sound.class, new SoundSerializer())
                .register(IntOr.Default.SERIALIZER)
                .register(IntOr.Disabled.SERIALIZER)
                .register(DoubleOr.Default.SERIALIZER)
                .register(DoubleOr.Disabled.SERIALIZER)
                .register(BooleanOrDefault.SERIALIZER)
                .register(Duration.SERIALIZER)
                .register(DurationOrDisabled.SERIALIZER);
    }
}
