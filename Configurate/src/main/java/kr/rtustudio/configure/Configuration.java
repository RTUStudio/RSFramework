package kr.rtustudio.configure;

import kr.rtustudio.configure.mapping.Definition;
import kr.rtustudio.configure.mapping.FieldProcessor;
import kr.rtustudio.configure.mapping.InnerClassFieldDiscoverer;
import kr.rtustudio.configure.mapping.MergeMap;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * {@link ConfigurationPart} 기반 YAML 설정 파일의 로드·저장·리로드를 담당하는 추상 클래스입니다.
 *
 * <p>Configurate {@link YamlConfigurationLoader}와 {@link ObjectMapper}를 사용하여 설정 객체를 YAML 노드에 매핑합니다.
 *
 * @param <T> 설정 타입
 */
@Slf4j(topic = "Configuration")
public abstract class Configuration<T extends ConfigurationPart> {

    public static final String VERSION_FIELD = "_version";
    protected final Class<T> type;
    protected final Path path;
    protected final BufferedReader defaultConfig;
    protected YamlConfigurationLoader loader;

    public Configuration(Class<T> type, Path path, BufferedReader defaultConfig) {
        this.type = type;
        this.path = path;
        this.defaultConfig = defaultConfig;
    }

    protected static <T> CheckedFunction<ConfigurationNode, T, SerializationException> creator(
            final Class<T> type, final boolean refreshNode) {
        return node -> {
            final T instance = node.require(type);
            if (refreshNode) {
                node.set(type, instance);
            }
            return instance;
        };
    }

    protected static <T> CheckedFunction<ConfigurationNode, T, SerializationException> reloader(
            Class<T> type, T instance) {
        return node -> {
            ObjectMapper.Factory factory =
                    (ObjectMapper.Factory)
                            Objects.requireNonNull(node.options().serializers().get(type));
            ObjectMapper.Mutable<T> mutable = (ObjectMapper.Mutable<T>) factory.get(type);
            mutable.load(instance, node);
            return instance;
        };
    }

    protected static ObjectMapper.Factory.Builder defaultFactoryBuilder(
            ObjectMapper.Factory.Builder builder) {
        return builder.addDiscoverer(InnerClassFieldDiscoverer.config(defaultFieldProcessors()));
    }

    private static List<Definition<?, ?, ? extends FieldProcessor.Factory<?, ?>>>
            defaultFieldProcessors() {
        return List.of(MergeMap.DEFINITION);
    }

    protected ObjectMapper.Factory.Builder createObjectMapper() {
        return ConfigurationSerializer.apply(ObjectMapper.factoryBuilder());
    }

    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.PRESERVE)
                .defaultOptions(ConfigurationSerializer::apply);
    }

    protected abstract boolean isConfigType(final Type type);

    protected abstract int configVersion();

    private void trySaveFileNode(ConfigurationNode node, String filename)
            throws ConfigurateException {
        try {
            loader.save(node);
        } catch (ConfigurateException ex) {
            if (ex.getCause() instanceof AccessDeniedException) {
                log.warn("Could not save {}", filename, ex);
            } else throw ex;
        }
    }

    protected T initializeConfiguration(
            final CheckedFunction<ConfigurationNode, T, SerializationException> creator)
            throws ConfigurateException {
        final YamlConfigurationLoader.Builder builder =
                this.createLoaderBuilder()
                        .defaultOptions(
                                this.applyObjectMapperFactory(
                                        defaultFactoryBuilder(this.createObjectMapper()).build()))
                        .path(path);
        if (this.defaultConfig == null) loader = builder.build();
        else loader = builder.source(() -> this.defaultConfig).build();
        final ConfigurationNode node;
        if (Files.notExists(path)) {
            if (this.defaultConfig == null) {
                node = CommentedConfigurationNode.root(loader.defaultOptions());
            } else node = loader.load();
            int version = this.configVersion();
            if (version > 0) node.node(VERSION_FIELD).raw(version);
        } else {
            node = loader.load();
            this.verifyConfigVersion(node);
        }
        final T instance = creator.apply(node);
        trySaveFileNode(node, path.toString());
        return instance;
    }

    protected void verifyConfigVersion(final ConfigurationNode globalNode) {
        final ConfigurationNode versionNode = globalNode.node(VERSION_FIELD);
        int version = this.configVersion();
        if (version < 1) return;
        if (versionNode.virtual()) {
            log.warn("The config file didn't have a version set, assuming latest");
            versionNode.raw(version);
        } else if (versionNode.getInt() > this.configVersion()) {
            log.error(
                    "Loading a newer configuration than is supported ({} > {})! You may have to backup & delete your config file to start the server.",
                    versionNode.getInt(),
                    this.configVersion());
        }
    }

    private UnaryOperator<ConfigurationOptions> applyObjectMapperFactory(
            final ObjectMapper.Factory factory) {
        return options ->
                options.serializers(
                        builder ->
                                builder.register(this::isConfigType, factory.asTypeSerializer())
                                        .registerAnnotatedObjects(factory));
    }
}
