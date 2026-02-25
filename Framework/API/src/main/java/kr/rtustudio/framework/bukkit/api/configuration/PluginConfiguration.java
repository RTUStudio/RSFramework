package kr.rtustudio.framework.bukkit.api.configuration;

import io.leangen.geantyref.GenericTypeReflector;
import kr.rtustudio.configure.Configuration;
import kr.rtustudio.configure.ConfigurationPart;
import kr.rtustudio.configure.ConfigurationSerializer;
import kr.rtustudio.framework.bukkit.api.RSPlugin;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * 플러그인별 YAML 설정 파일을 로드·리로드하는 구현체입니다.
 *
 * <p>플러그인 정보를 포함한 헤더, 버전 관리, 커스텀 직렬화를 지원합니다.
 *
 * @param <C> 설정 타입
 */
public class PluginConfiguration<C extends ConfigurationPart> extends Configuration<C> {

    public static final String HEADER =
            """
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
    private final Consumer<TypeSerializerCollection.Builder> extraSerializer;

    public PluginConfiguration(
            RSPlugin plugin,
            Class<C> type,
            Path path,
            BufferedReader defaultConfig,
            Integer version,
            Consumer<TypeSerializerCollection.Builder> extraSerializer)
            throws ConfigurateException {
        super(type, path, defaultConfig);
        this.plugin = plugin;
        this.version = version;
        this.extraSerializer = extraSerializer;
    }

    private ConfigurationOptions defaultOptions(ConfigurationOptions options) {
        String name = plugin.getName();
        List<String> authors = plugin.getDescription().getAuthors();
        String author = authors.getFirst();

        String header = String.format(HEADER, name, author, name, author);
        if (this.extraSerializer == null) {
            return ConfigurationSerializer.apply(options.header(header));
        }
        return ConfigurationSerializer.apply(options.header(header), this.extraSerializer);
    }

    @Override
    protected int configVersion() {
        return version == null ? 0 : version;
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder().defaultOptions(this::defaultOptions);
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(GenericTypeReflector.erase(type));
    }

    /**
     * 설정 파일을 로드하여 새 인스턴스를 생성한다.
     *
     * @return 로드된 설정 인스턴스
     */
    public C load() {
        try {
            return this.initializeConfiguration(creator(this.type, true));
        } catch (Exception ex) {
            throw new RuntimeException("Could not load configuration files", ex);
        }
    }

    /**
     * 기존 인스턴스에 파일 내용을 다시 로드한다.
     *
     * @param instance 리로드할 기존 인스턴스
     */
    public void reload(C instance) {
        try {
            this.initializeConfiguration(reloader(this.type, instance));
        } catch (Exception ex) {
            throw new RuntimeException("Could not reload configuration files", ex);
        }
    }
}
