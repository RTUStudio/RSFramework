package kr.rtuserver.framework.bukkit.api.configuration;

import io.leangen.geantyref.GenericTypeReflector;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.serializer.ComponentSerializer;
import kr.rtuserver.framework.bukkit.api.configuration.serializer.EnumValueSerializer;
import kr.rtuserver.framework.bukkit.api.configuration.serializer.collection.map.FlattenedMapSerializer;
import kr.rtuserver.framework.bukkit.api.configuration.serializer.collection.map.MapSerializer;
import kr.rtuserver.framework.bukkit.api.configuration.type.BooleanOrDefault;
import kr.rtuserver.framework.bukkit.api.configuration.type.Duration;
import kr.rtuserver.framework.bukkit.api.configuration.type.DurationOrDisabled;
import kr.rtuserver.framework.bukkit.api.configuration.type.number.DoubleOr;
import kr.rtuserver.framework.bukkit.api.configuration.type.number.IntOr;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;

public class PluginConfiguration<C extends ConfigurationPart> extends Configuration<C> {

    public static final String HEADER = """
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

    private final RSPlugin plugin;

    private final Integer version;

    public PluginConfiguration(RSPlugin plugin, Class<C> type, Path path, BufferedReader defaultConfig, Integer version) throws ConfigurateException {
        super(type, path, defaultConfig);
        this.plugin = plugin;
        this.version = version;
    }

    private ConfigurationOptions defaultOptions(ConfigurationOptions options) {
        String name = plugin.getName();
        List<String> authors = plugin.getDescription().getAuthors();
        String author = authors.getFirst();

        String header = String.format(HEADER, name, author, name, author);
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
    protected int configVersion() {
        return version == null ? 0 : version;
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder()
                .defaultOptions(this::defaultOptions);
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(GenericTypeReflector.erase(type));
    }

    public C load() {
        try {
            return this.initializeConfiguration(creator(this.type, true));
        } catch (Exception ex) {
            throw new RuntimeException("Could not load configuration files", ex);
        }
    }

    public void reload(C instance) {
        try {
            this.initializeConfiguration(reloader(this.type, instance));
        } catch (Exception ex) {
            throw new RuntimeException("Could not reload configuration files", ex);
        }
    }
}
