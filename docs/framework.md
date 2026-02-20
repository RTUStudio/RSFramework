# Framework

`Framework:API`와 `Framework:Core`로 구성된 Bukkit 플러그인 개발 핵심 모듈입니다.
`RSPlugin`을 상속하면 설정, 브로커, 스토리지, 명령어, 이벤트, NMS, 모듈/프로바이더 시스템 등 엔터프라이즈급 기능을 손쉽게 사용할 수 있습니다.

---

## 1. RSPlugin

`kr.rtustudio.framework.bukkit.api.RSPlugin`
기존 `JavaPlugin`을 확장한 추상 클래스로, 플러그인의 전체 라이프사이클과 핵심 서비스를 관리합니다.

### 라이프사이클 훅

| 메서드 | 호출 시점 | 설명 |
|--------|-----------|------|
| `initialize()` | `onLoad()` 시작 시 | DI 컨테이너 및 기초 환경 초기화 전 수행 |
| `load()` | `onLoad()` 말미 | 설정(Configuration) 등록, 스토리지 타입 지정 및 등록 |
| `enable()` | `onEnable()` | 이벤트 리스너, 명령어 등록 및 플러그인 주 로직 실행 |
| `disable()` | `onDisable()` | 데이터 저장, 연결 해제 등 정리 작업 |

### 사용 예시

```java
public class MyPlugin extends RSPlugin {

    private MyConfig config;
    private ConfigList<RegionConfig> regions;

    @Override
    protected void load() {
        // 단일 파일: Config/MyConfig.yml
        config = registerConfiguration(MyConfig.class, ConfigPath.of("MyConfig"));

        // 폴더 전체: Config/Regions/*.yml
        regions = registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));

        // 스토리지 타입 지정 및 등록
        registerStorage("LocalData");                          // 미지정 시 기본 JSON
        registerStorage("RemoteData", StorageType.MYSQL);      // MySQL로 지정
    }

    @Override
    protected void enable() {
        registerEvent(new MyListener(this));
        registerCommand(new MyCommand(this));

        // 스토리지 사용
        Storage local = getStorage("LocalData");
        Storage mysql = getStorage("RemoteData");
    }
}
```

### 주요 API 메서드

```java
// 단일 설정 파일 등록
T registerConfiguration(Class<T> config, ConfigPath path)
T registerConfiguration(Class<T> config, ConfigPath path, Consumer<TypeSerializerCollection.Builder> serializers)

// 폴더 내 전체 .yml 등록 → ConfigList<T>
ConfigList<T> registerConfigurations(Class<T> config, ConfigPath path)
ConfigList<T> registerConfigurations(Class<T> config, ConfigPath path, Consumer<...> serializers)

// 이미 등록된 설정 조회
T getConfiguration(Class<T> config)
boolean reloadConfiguration(Class<? extends ConfigurationPart> config)

// 스토리지 (name별 다중 스토리지)
void registerStorage(String name)                     // 기본 JSON 등록
void registerStorage(String name, StorageType type)   // 타입 지정 등록
Storage getStorage(String name)                       // 인스턴스 조회

// 브로커
T getBroker(Class<T> type)           // ProtoWeaver, RedisBroker 등 특정 타입 조회

// 프로바이더
T getProvider(Class<T> type)
void setProvider(Class<T> type, T provider)

// 이벤트/명령어
void registerEvent(RSListener<?> listener)
void registerCommand(RSCommand<?> command)            // 기본 등록
void registerCommand(RSCommand<?> command, boolean reload) // reload 커맨드 여부

// 로그 출력 (ComponentFormatter 내장)
void console(Component message)      // Adventure Component
void console(String minimessage)     // MiniMessage 문자열 포맷
void verbose(Component message)      // setting.verbose=true 일 때만 출력
void verbose(String minimessage)
```

---

## 2. 설정 경로 (ConfigPath)

`kr.rtustudio.framework.bukkit.api.configuration.ConfigPath`
Spongepowered Configurate 기반의 설정 파일 경로를 유연하게 관리하는 인터페이스입니다.

### 2.1. `of()` — 자동 접두사 (Config/)
일반적인 설정 파일은 `Config/` 폴더 하위에 위치해야 합니다. `of()`를 사용하면 자동으로 접두사가 붙습니다.

```java
// 단일 파일: Config/Setting.yml
ConfigPath.of("Setting")

// 단일 파일 (서브 폴더): Config/Storage/MySQL.yml
ConfigPath.of("Storage", "MySQL")

// 복수 파일 폴더: Config/Regions/
ConfigPath.of("Regions")
```

### 2.2. `relative()` — 접두사 없음
플러그인 루트 폴더(`plugins/<Plugin>/`)를 기준으로 임의의 경로를 지정할 때 사용합니다.

```java
// 단일 파일: Broker/Redis.yml
ConfigPath.relative("Broker", "Redis")

// 서브 폴더: Translation/Message/ko.yml
ConfigPath.relative("Translation", "Message", "ko")
```

### 2.3. 단수/복수 해석 규칙
* **단수 (`registerConfiguration`)**: 경로의 `last()`가 파일명이 되고(`.yml` 자동 부착), 그 앞부분이 폴더가 됩니다.
* **복수 (`registerConfigurations`)**: 지정한 전체 경로가 폴더가 되며, 해당 폴더 내의 모든 `.yml` 파일이 로드됩니다.

---

## 3. RSConfiguration & ConfigurationPart

Spongepowered Configurate 기반 YAML 설정 매핑 클래스입니다.

### 3.1. 설정 모델 정의 (`ConfigurationPart`)

```java
@Getter
@SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "NotNullFieldNotInitialized"})
public class MyConfig extends ConfigurationPart {

    @Comment("최대 플레이어 수")
    private int maxPlayers = 100;

    @Comment("기능 활성화 여부")
    private boolean enabled = true;

    // 중첩 설정 클래스 지원
    public Nested nested;

    @Getter
    public class Nested extends ConfigurationPart {
        private String value = "default";
    }
}
```

### 3.2. 단일 파일 등록
```java
// Config/MyConfig.yml
MyConfig cfg = plugin.registerConfiguration(MyConfig.class, ConfigPath.of("MyConfig"));
```

### 3.3. 폴더 전체 등록 (`ConfigList<T>`)
폴더 내의 모든 `.yml` 파일을 스캔하여 키-값 형태의 `ConfigList`로 관리합니다.

```java
// Config/Regions/*.yml
ConfigList<RegionConfig> regions = plugin.registerConfigurations(
    RegionConfig.class, ConfigPath.of("Regions")
);

// Config/Regions/spawn.yml 접근
RegionConfig spawn = regions.get("spawn");

// 순회
for (RegionConfig r : regions.values()) {
    System.out.println(r.getName());
}
```

---

## 4. Module & Provider 시스템

### 4.1. Module (전역 설정 모듈)
프레임워크 내부에서 제공하는 플러그인 전역 설정값을 관리합니다.

* **CommandModule** (`Modules/Command.yml`): 명령어 쿨다운 등 관리
* **ThemeModule** (`Modules/Theme.yml`): 메시지 접두사(Prefix), 그라데이션 색상 등 관리

```java
ThemeModule theme = plugin.getFramework().getModule(ThemeModule.class);
String prefix = theme.getPrefix();
```

### 4.2. Provider (교체 가능한 서비스)
런타임에 구현체를 교체할 수 있는 서비스 제공자 패턴입니다.

* **NameProvider**: 플레이어 이름 조회를 추상화 (기본 `VanillaNameProvider`). 타 플러그인(닉네임 시스템 등)에서 교체하여 사용할 수 있습니다.

```java
NameProvider nameProvider = plugin.getProvider(NameProvider.class);
String name = nameProvider.getName(uuid);

// 커스텀 프로바이더로 교체
plugin.setProvider(NameProvider.class, new CustomNickNameProvider());
```

---

## 5. 기타 유틸리티

### 5.1. RSListener
이벤트 리스너를 간결하게 작성할 수 있도록 돕는 추상 클래스입니다.

```java
public class PlayerJoinListener extends RSListener<MyPlugin> {
    public PlayerJoinListener(MyPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin().console("<green>" + e.getPlayer().getName() + " joined</green>");
    }
}
```

### 5.2. 통합 Scheduler
Folia의 비동기 환경과 Paper/Bukkit의 동기 환경을 하나로 추상화한 통합 스케줄러를 제공합니다.

```java
CraftScheduler scheduler = plugin.getFramework().getScheduler();

// 플랫폼 독립적인 반복 작업 (예: 20틱마다 실행)
scheduler.repeat(plugin, () -> {
    // 로직
}, 0L, 20L);
```
