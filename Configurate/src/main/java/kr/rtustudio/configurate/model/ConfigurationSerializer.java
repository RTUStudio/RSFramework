package kr.rtustudio.configurate.model;

import kr.rtustudio.configurate.model.constraint.Constraint;
import kr.rtustudio.configurate.model.constraint.Constraints;
import kr.rtustudio.configurate.model.serializer.ComponentSerializer;
import kr.rtustudio.configurate.model.serializer.EnumValueSerializer;
import kr.rtustudio.configurate.model.serializer.collection.map.FlattenedMapSerializer;
import kr.rtustudio.configurate.model.serializer.collection.map.MapSerializer;
import kr.rtustudio.configurate.model.type.BooleanOrDefault;
import kr.rtustudio.configurate.model.type.Duration;
import kr.rtustudio.configurate.model.type.DurationOrDisabled;
import kr.rtustudio.configurate.model.type.number.DoubleOr;
import kr.rtustudio.configurate.model.type.number.IntOr;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;

/**
 * {@link ConfigurationOptions}와 {@link ObjectMapper.Factory.Builder}에 내장 직렬화기 및 제약 조건을 일괄 등록하는
 * 유틸리티.
 *
 * <p>등록되는 직렬화기: {@link kr.rtustudio.configurate.model.serializer.ComponentSerializer
 * ComponentSerializer}, {@link kr.rtustudio.configurate.model.serializer.EnumValueSerializer
 * EnumValueSerializer}, {@link
 * kr.rtustudio.configurate.model.serializer.collection.map.MapSerializer MapSerializer}, {@link
 * kr.rtustudio.configurate.model.serializer.collection.map.FlattenedMapSerializer
 * FlattenedMapSerializer}, {@link kr.rtustudio.configurate.model.type.number.IntOr IntOr}, {@link
 * kr.rtustudio.configurate.model.type.number.DoubleOr DoubleOr}, {@link
 * kr.rtustudio.configurate.model.type.BooleanOrDefault BooleanOrDefault}, {@link
 * kr.rtustudio.configurate.model.type.Duration Duration}, {@link
 * kr.rtustudio.configurate.model.type.DurationOrDisabled DurationOrDisabled}
 *
 * <p>등록되는 제약 조건: {@link kr.rtustudio.configurate.model.constraint.Constraint @Constraint}, {@link
 * kr.rtustudio.configurate.model.constraint.Constraints.Min @Min}, {@link
 * kr.rtustudio.configurate.model.constraint.Constraints.Max @Max}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationSerializer {

    public static ConfigurationOptions apply(ConfigurationOptions options) {
        return options.serializers(ConfigurationSerializer::registerSerializers)
                .mapFactory(MapFactories.insertionOrdered());
    }

    /**
     * 내장 직렬화와 추가 직렬화를 함께 적용한다.
     *
     * @param options 기존 옵션
     * @param extra 추가 타입 직렬화
     * @return 직렬화가 적용된 옵션
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
                .register(IntOr.Default.SERIALIZER)
                .register(IntOr.Disabled.SERIALIZER)
                .register(DoubleOr.Default.SERIALIZER)
                .register(DoubleOr.Disabled.SERIALIZER)
                .register(BooleanOrDefault.SERIALIZER)
                .register(Duration.SERIALIZER)
                .register(DurationOrDisabled.SERIALIZER);
    }
}
