package kr.rtustudio.framework.bukkit.api;

import kr.rtustudio.broker.Broker;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigList;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.core.module.ThemeModule;
import kr.rtustudio.framework.bukkit.api.core.provider.Provider;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.integration.Integration;
import kr.rtustudio.framework.bukkit.api.library.LibraryLoader;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

/**
 * 플러그인의 메인 클래스가 상속해야 하는 추상 클래스입니다.
 *
 * <p>명령어, 이벤트, 설정, 스토리지 및 각종 모듈의 등록과 생명주기를 관리합니다. Bukkit {@link JavaPlugin}을 확장하며, {@link
 * #initialize()}, {@link #load()}, {@link #enable()}, {@link #disable()} 콜백을 통해 플러그인 라이프사이클에 개입할 수
 * 있습니다.
 */
@Getter
@SuppressWarnings("unused")
public abstract class RSPlugin extends JavaPlugin {

    private static final String MINIMUM_SUPPORTED_VERSION = "1.20.1";

    private final Set<RSListener<? extends RSPlugin>> listeners = new HashSet<>();
    private final Set<RSCommand<? extends RSPlugin>> commands = new HashSet<>();
    private final Set<Integration> integrations = new HashSet<>();
    private final LinkedHashSet<String> languages = new LinkedHashSet<>();

    private final LibraryLoader libraryLoader;

    private Framework framework;
    private Component prefix;
    private BukkitAudiences adventure;
    private RSConfiguration configuration;

    /** 기본 로케일({@code en_us}, {@code ko_kr})로 플러그인을 생성한다. */
    public RSPlugin() {
        this("en_us", "ko_kr");
    }

    /**
     * 지정한 로케일 목록으로 플러그인을 생성한다.
     *
     * @param languages 지원할 로케일 코드 (첫 번째가 기본 로케일)
     */
    public RSPlugin(String... languages) {
        Collections.addAll(this.languages, languages);
        this.libraryLoader = new LibraryLoader(this);
    }

    /** 설정 파일에서 불러온 플러그인의 접두사(Prefix) 컴포넌트를 반환합니다. */
    public Component getPrefix() {
        String str = this.configuration.getSetting().getPrefix();
        if (str.isEmpty()) return this.prefix;
        return ComponentFormatter.mini(str);
    }

    @Override
    public void onEnable() {
        if (!MinecraftVersion.isSupport(MINIMUM_SUPPORTED_VERSION)) {
            Bukkit.getLogger()
                    .warning(
                            "Server version is unsupported version (< "
                                    + MINIMUM_SUPPORTED_VERSION
                                    + "), Disabling this plugin...");
            Bukkit.getLogger()
                    .warning(
                            "서버 버전이 지원되지 않는 버전입니다 (< "
                                    + MINIMUM_SUPPORTED_VERSION
                                    + "), 플러그인을 비활성화합니다...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.adventure = BukkitAudiences.create(this);
        registerPermission("command.reload", PermissionDefault.OP);
        this.getDescription().getSoftDepend().forEach(this.framework::hookDependency);
        enable();
        console("<green>Enable!</green>");
        this.framework.loadPlugin(this);
    }

    @Override
    public void onDisable() {
        this.integrations.forEach(Integration::unregister);
        disable();
        if (this.framework != null) this.framework.unloadPlugin(this);
        console("<red>Disable!</red>");
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public void onLoad() {
        this.framework = LightDI.getBean(Framework.class);
        this.configuration = new RSConfiguration(this);
        initialize();
        ThemeModule theme = this.framework.getModule(ThemeModule.class);
        this.prefix =
                ComponentFormatter.mini(
                        "<gradient:%s:%s>%s%s%s</gradient>"
                                .formatted(
                                        theme.getGradientStart(),
                                        theme.getGradientEnd(),
                                        theme.getPrefix(),
                                        getName(),
                                        theme.getSuffix()));
        load();
    }

    /**
     * 디버그(verbose) 모드일 때만 콘솔에 메시지를 출력한다.
     *
     * @param message 출력할 컴포넌트
     */
    public void verbose(Component message) {
        if (this.configuration.getSetting().isVerbose()) console(message);
    }

    /**
     * 디버그(verbose) 모드일 때만 콘솔에 MiniMessage 형식 메시지를 출력한다.
     *
     * @param minimessage MiniMessage 형식 문자열
     */
    public void verbose(String minimessage) {
        verbose(ComponentFormatter.mini(minimessage));
    }

    /**
     * 플러그인 접두사와 함께 콘솔에 메시지를 출력한다.
     *
     * @param message 출력할 컴포넌트
     */
    public void console(Component message) {
        if (this.adventure != null) {
            this.adventure.console().sendMessage(getPrefix().append(message));
        }
    }

    /**
     * 플러그인 접두사와 함께 콘솔에 MiniMessage 형식 메시지를 출력한다.
     *
     * @param minimessage MiniMessage 형식 문자열
     */
    public void console(String minimessage) {
        console(ComponentFormatter.mini(minimessage));
    }

    /**
     * Maven 형식의 라이브러리를 런타임에 동적으로 로드한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} 형식 문자열
     */
    public void loadLibrary(String dependency) {
        this.libraryLoader.load(dependency);
    }

    /**
     * 패키지 재배치(relocation)를 적용하여 라이브러리를 런타임에 로드한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} 형식 문자열
     * @param pattern 원본 패키지 패턴
     * @param relocatedPattern 재배치 대상 패키지 패턴
     */
    public void loadLibrary(String dependency, String pattern, String relocatedPattern) {
        this.libraryLoader.load(dependency, pattern, relocatedPattern);
    }

    /**
     * 등록된 설정 인스턴스를 타입으로 조회한다.
     *
     * @param configuration 조회할 {@link ConfigurationPart} 클래스
     * @param <T> 설정 타입
     * @return 등록된 설정 인스턴스, 없으면 {@code null}
     */
    public <T extends ConfigurationPart> T getConfiguration(Class<T> configuration) {
        return this.configuration.get(configuration);
    }

    /**
     * 등록된 설정 목록 인스턴스를 타입으로 조회한다.
     *
     * @param configuration 조회할 {@link ConfigurationPart} 클래스
     * @param <T> 설정 타입
     * @return 등록된 설정 목록 인스턴스, 없으면 빈 {@link ConfigList}
     */
    public <T extends ConfigurationPart> ConfigList<T> getConfigurations(Class<T> configuration) {
        return this.configuration.getList(configuration);
    }

    /**
     * 지정한 설정을 파일에서 다시 로드한다.
     *
     * @param configuration 리로드할 {@link ConfigurationPart} 클래스
     * @return 리로드 성공 여부
     */
    public boolean reloadConfiguration(Class<? extends ConfigurationPart> configuration) {
        return this.configuration.reload(configuration);
    }

    /**
     * 단일 YAML 설정 파일을 등록하고 로드한다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 설정 경로
     * @param <T> 설정 타입
     * @return 로드된 설정 인스턴스
     */
    public <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration, ConfigPath path) {
        return this.configuration.registerConfiguration(configuration, path);
    }

    /**
     * 커스텀 직렬화를 포함하여 단일 YAML 설정 파일을 등록하고 로드한다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 설정 경로
     * @param serializers 추가 타입 직렬화
     * @param <T> 설정 타입
     * @return 로드된 설정 인스턴스
     */
    public <T extends ConfigurationPart> T registerConfiguration(
            Class<T> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.registerConfiguration(configuration, path, serializers);
    }

    /**
     * 폴더 내 모든 YAML 파일을 개별 설정 인스턴스로 등록한다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 폴더 경로
     * @param <T> 설정 타입
     * @return 파일명을 키로 하는 {@link ConfigList}
     */
    public <T extends ConfigurationPart> ConfigList<T> registerConfigurations(
            Class<T> configuration, ConfigPath path) {
        return this.configuration.registerConfigurations(configuration, path);
    }

    /**
     * 커스텀 직렬화를 포함하여 폴더 내 모든 YAML 파일을 등록한다.
     *
     * @param configuration 로드할 {@link ConfigurationPart} 클래스
     * @param path 폴더 경로
     * @param serializers 추가 타입 직렬화
     * @param <T> 설정 타입
     * @return 파일명을 키로 하는 {@link ConfigList}
     */
    public <T extends ConfigurationPart> ConfigList<T> registerConfigurations(
            Class<T> configuration,
            ConfigPath path,
            Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.configuration.registerConfigurations(configuration, path, serializers);
    }

    /**
     * 등록된 스토리지 인스턴스를 반환한다.
     *
     * @param name 스토리지 식별 이름 (예: {@code "Local"}, {@code "LocalSQL"})
     * @return 해당 이름의 {@link Storage}, 등록되지 않았으면 {@code null}
     */
    public Storage getStorage(@NotNull String name) {
        return this.framework.getStorage(this, name);
    }

    /**
     * 지정한 이름과 타입으로 스토리지를 등록한다.
     *
     * <pre>{@code
     * registerStorage("LocalSQL", StorageType.SQLITE);
     * registerStorage("Local", StorageType.JSON);
     * }</pre>
     *
     * @param name 스토리지 식별 이름
     * @param type 기본 스토리지 타입
     */
    protected void registerStorage(@NotNull String name, @NotNull StorageType type) {
        this.framework.registerStorage(this, name, type);
    }

    /**
     * 지정한 이름으로 스토리지를 등록한다. 기본 타입은 {@link StorageType#JSON}.
     *
     * @param name 스토리지 식별 이름
     */
    protected void registerStorage(@NotNull String name) {
        this.framework.registerStorage(this, name, StorageType.JSON);
    }

    /**
     * 등록된 브로커 인스턴스를 타입으로 조회한다.
     *
     * @param type 브로커 인터페이스 클래스
     * @param <T> 브로커 타입
     * @return 등록된 브로커 인스턴스
     */
    public <T extends Broker> T getBroker(@NotNull Class<T> type) {
        return this.framework.getBroker(type);
    }

    /**
     * 등록된 프로바이더 인스턴스를 타입으로 조회한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @param <T> 프로바이더 타입
     * @return 등록된 프로바이더 인스턴스, 없으면 {@code null}
     */
    public <T extends Provider> T getProvider(@NotNull Class<T> type) {
        return this.framework.getProvider(type);
    }

    /**
     * 프로바이더 인스턴스를 등록하거나 교체한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @param provider 등록할 프로바이더 인스턴스
     * @param <T> 프로바이더 타입
     */
    public <T extends Provider> void setProvider(@NotNull Class<T> type, @NotNull T provider) {
        this.framework.setProvider(type, provider);
    }

    /**
     * 이벤트 리스너를 등록한다.
     *
     * @param listener 등록할 {@link RSListener}
     */
    protected void registerEvent(RSListener<? extends RSPlugin> listener) {
        this.listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /** 등록된 모든 이벤트 리스너를 다시 활성화한다. */
    public void registerEvents() {
        this.listeners.forEach(
                listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    /** 이 플러그인에 등록된 모든 이벤트 리스너를 해제한다. */
    public void unregisterEvents() {
        HandlerList.getHandlerLists().forEach(handler -> handler.unregister(this));
    }

    /**
     * 명령어를 등록한다.
     *
     * @param command 등록할 {@link RSCommand}
     */
    protected void registerCommand(RSCommand<? extends RSPlugin> command) {
        registerCommand(command, false);
    }

    /**
     * 명령어를 등록한다.
     *
     * @param command 등록할 {@link RSCommand}
     * @param reload {@code true}이면 서버 명령어 맵을 즉시 갱신한다
     */
    protected void registerCommand(RSCommand<? extends RSPlugin> command, boolean reload) {
        this.commands.add(command);
        this.framework.registerCommand(command, reload);
    }

    /**
     * 발신자가 이 플러그인의 특정 권한을 가지고 있는지 확인한다.
     *
     * <p>권한 노드는 {@code <플러그인명>.<permission>} 형식으로 조합된다.
     *
     * @param sender 확인할 대상
     * @param permission 권한 접미사 (예: {@code "command.reload"})
     * @return 권한 보유 여부
     */
    public boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(getName().toLowerCase() + "." + permission.toLowerCase());
    }

    /**
     * 이 플러그인의 권한 노드를 서버에 등록한다.
     *
     * @param permission 권한 접미사 (예: {@code "command.reload"})
     * @param permissionDefault 기본 권한 수준
     */
    public void registerPermission(String permission, PermissionDefault permissionDefault) {
        String name = getName() + "." + permission;
        this.framework.registerPermission(name.toLowerCase(), permissionDefault);
    }

    /**
     * 외부 플러그인 연동(Integration)을 등록한다.
     *
     * <p>연동 대상이 사용 불가능하면 무시된다.
     *
     * @param integrationWrapper 등록할 연동 래퍼
     */
    protected void registerIntegration(Integration integrationWrapper) {
        if (!integrationWrapper.isAvailable()) return;
        this.integrations.add(integrationWrapper);
        integrationWrapper.register();
    }

    /**
     * 플러그인 초기화 콜백. {@code onLoad()} 초반에 호출된다.
     *
     * <p>라이브러리 로드, 설정 등록 등 프레임워크 초기화 전에 수행할 작업을 여기에 구현한다.
     */
    protected void initialize() {}

    /**
     * 플러그인 로드 콜백. {@code onLoad()} 후반에 호출된다.
     *
     * <p>프레임워크 초기화가 완료된 후 수행할 작업을 여기에 구현한다.
     */
    protected void load() {}

    /**
     * 플러그인 활성화 콜백. {@code onEnable()} 내부에서 호출된다.
     *
     * <p>명령어, 이벤트, 스토리지 등록 등을 여기에 구현한다.
     */
    protected void enable() {}

    /**
     * 플러그인 비활성화 콜백. {@code onDisable()} 내부에서 호출된다.
     *
     * <p>리소스 정리 등 종료 작업을 여기에 구현한다.
     */
    protected void disable() {}
}
