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

/**
 * Configurate의 {@link ConfigurationOptions} 및 {@link ObjectMapper.Factory.Builder}에 프레임워크 내장 직렬화/제약
 * 조건을 일괄 적용하는 유틸리티 클래스입니다.
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
