package kr.rtuserver.framework.bukkit.api.configuration.paper.configuration;

import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.serializer.*;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.serializer.collection.map.FlattenedMapSerializer;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.serializer.collection.map.MapSerializer;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.type.*;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.type.number.DoubleOr;
import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.type.number.IntOr;
import org.spongepowered.configurate.*;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.file.Path;

import static io.leangen.geantyref.GenericTypeReflector.erase;

@SuppressWarnings("Convert2Diamond")
public class ConfigurationImpl<T extends ConfigurationPart> extends Configuration<T> {

    private static final String HEADER = """
            ╔ Developed by ════════════════════════════════════╗
            ║ ░█▀▄░░▀█▀░░█░█░░░░░█▀▀░░▀█▀░░█░█░░█▀▄░░▀█▀░░█▀█░ ║
            ║ ░█▀▄░░░█░░░█░█░░░░░▀▀█░░░█░░░█░█░░█░█░░░█░░░█░█░ ║
            ║ ░▀░▀░░░▀░░░▀▀▀░░░░░▀▀▀░░░▀░░░▀▀▀░░▀▀░░░▀▀▀░░▀▀▀░ ║
            ╚══════════════════════════════════════════════════╝
            
            This is the configuration for %s.
            If you have any questions or need assistance,
            please join our Discord server and ask for help from %s!
            
            이것은 %s의 구성입니다.
            질문이 있거나 도움이 필요하시면,
            저희 Discord 서버에 가입하셔서 %s에게 도움을 요청해 주세요!""";

    private final Integer version;

    public ConfigurationImpl(Class<T> type, Path path, BufferedReader defaultConfig, Integer version) throws ConfigurateException {
        super(type, path, defaultConfig);
        this.version = version;
    }

    @Override
    protected int configVersion() {
        return version == null ? 0 : version;
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder()
                .defaultOptions(ConfigurationImpl::defaultOptions);
    }

    private static ConfigurationOptions defaultOptions(ConfigurationOptions options) {
        String header = String.format(HEADER, "WIP", "WIP", "WIP", "WIP");
        return options.header(header).serializers(builder -> builder
                .register(FlattenedMapSerializer.TYPE, new FlattenedMapSerializer(false))
                .register(MapSerializer.TYPE, new MapSerializer(false))
                .register(new EnumValueSerializer())
                .register(new ComponentSerializer())
                .register(IntOr.Default.SERIALIZER)
                .register(IntOr.Disabled.SERIALIZER)
                .register(DoubleOr.Default.SERIALIZER)
                .register(DoubleOr.Disabled.SERIALIZER)
                .register(BooleanOrDefault.SERIALIZER)
                .register(Duration.SERIALIZER)
                .register(DurationOrDisabled.SERIALIZER)
        );
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(erase(type));
    }

    public T load() {
        try {
            return this.initializeConfiguration(creator(this.type, true));
        } catch (Exception ex) {
            throw new RuntimeException("Could not load paper configuration files", ex);
        }
    }

    public void reload(T instance) {
        try {
            this.initializeConfiguration(reloader(this.type, instance));
        } catch (Exception ex) {
            throw new RuntimeException("Could not reload paper configuration files", ex);
        }
    }
}
