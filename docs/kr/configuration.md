# RSFramework Configuration 시스템

객체 지향 YAML 설정 매핑 시스템입니다. SpongePowered Configurate 기반으로, 자바 클래스 필드와 YAML을 직접 매핑합니다.

---

## 1. 아키텍처

```text
Configurate/                                   플랫폼 독립 모듈
└── kr.rtustudio.configurate.model
    ├── ConfigurationPart                      설정 객체 베이스 클래스
    ├── Configuration                          YAML 로드·저장·리로드 처리
    ├── ConfigPath                             설정 파일 경로 정의
    ├── ConfigList                             복수 YAML 파일 래퍼
    ├── constraint/                            제약 어노테이션 (@Min, @Max 등)
    ├── mapping/                               객체 매핑 헬퍼
    ├── serializer/                            커스텀 직렬화기
    └── type/                                  특수 타입 (Duration, BooleanOrDefault 등)
```

---

## 2. 기본 사용법

### 2.1. 설정 클래스 정의

모든 설정 클래스는 `ConfigurationPart`를 상속합니다.

> [!IMPORTANT]
> **`@SuppressWarnings` 필수** — Configurate는 리플렉션으로 필드를 직접 조작합니다.
> ```java
> @SuppressWarnings({
>     "unused",              // 리플렉션 접근이므로 직접 참조 없음
>     "CanBeFinal",          // 역직렬화 시 값을 주입
>     "FieldCanBeLocal",     // 필드가 YAML에 매핑되어야 함
>     "FieldMayBeFinal",     // 역직렬화 시 변경 가능해야 함
>     "InnerClassMayBeStatic" // 내부 클래스가 외부 인스턴스에 바인딩되어야 함
> })
> ```

```java
import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.configurate.model.constraint.Constraints.Min;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class MySettings extends ConfigurationPart {

    @Comment("Maximum number of players allowed in the lobby")
    @Min(1)
    private int maxPlayers = 50;

    @Comment("Welcome message displayed on join (MiniMessage format)")
    private String welcomeMessage = "<green>서버에 오신 것을 환영합니다!</green>";

    @Comment("Enable debug logging")
    private boolean verbose = false;
}
```

### 2.2. 등록

```java
@Override
protected void initialize() {
    // 단일 설정 파일 → plugins/MyPlugin/config.yml
    registerConfiguration(MySettings.class, ConfigPath.of("config"));
    
    // 폴더 내 모든 YAML → plugins/MyPlugin/classes/*.yml
    registerConfigurations(ClassConfig.class, ConfigPath.of("classes"));
}
```

### 2.3. 런타임 접근 및 리로드

```java
MySettings settings = getConfiguration(MySettings.class);
int maxPlayers = settings.getMaxPlayers();

// 리로드
reloadConfiguration(MySettings.class);
```

---

## 3. 리로드 안전성 (Reload Safety)

설정 리로드 시 Configurate의 `ObjectMapper.Mutable.load()`는 기존 컬렉션 객체를 재활용하여 `clear()` → `addAll()` / `putAll()`로 내용을 갱신합니다. 따라서 `List.of()`, `Map.of()` 등의 **불변(Immutable) 컬렉션**을 기본값으로 사용하면 리로드 시 `UnsupportedOperationException`이 발생합니다.

`ConfigurationPart`는 이를 방지하기 위해 가변(Mutable) 컬렉션을 생성하는 헬퍼 메서드를 제공합니다. 두 헬퍼 모두 **삽입 순서를 보존**합니다 — `listOf()`는 `ArrayList`를, `mapOf()`는 `LinkedHashMap`을 사용하므로 코드에서 선언한 순서가 생성되는 YAML에 그대로 반영됩니다.

### 3.1. `listOf()` — 가변 리스트 (순서 보존)

```java
// varargs: 간단한 초기화
private List<String> commands = listOf("help", "spawn", "home");

// Consumer: 복잡한 초기화
private List<String> rewards = listOf(list -> {
    list.add("diamond:10");
    list.add("gold_ingot:64");
});
```

### 3.2. `mapOf()` — 가변 맵 (순서 보존)

```java
// key-value 방식 (0~5개 엔트리)
private Map<String, Integer> prices = mapOf("diamond", 100, "iron_ingot", 10);

// Consumer 방식: 복잡한 초기화
private Map<String, List<String>> groups = mapOf(map -> {
    map.put("default", listOf("help"));
    map.put("admin", listOf("help", "ban", "kick"));
});

// 빈 맵
private Map<String, String> aliases = mapOf();
```

### 3.3. `make()` — 범용 초기화

임의 객체에 대해 생성 후 초기화 로직을 적용할 수 있습니다.

```java
private Set<String> blockedWorlds = make(new HashSet<>(), set -> {
    set.add("lobby");
    set.add("hub");
});
```

> [!WARNING]
> `List.of()`, `Map.of()`, `Set.of()`, `Collections.unmodifiableList()` 등 불변 컬렉션을 설정 필드 기본값으로 사용하지 마세요. 리로드 시 서버 오류가 발생합니다.

---

## 4. 계층 구조 (내부 클래스)

YAML의 중첩 구조를 자바 내부 클래스로 매핑합니다.

```java
@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class ProxiumConfig extends ConfigurationPart {

    @Comment("""
            TLS encryption settings
            TLS 암호화 설정""")
    public Tls tls;

    @Comment("Compression algorithm (NONE, GZIP, SNAPPY, FAST_LZ)")
    private CompressionType compression = CompressionType.SNAPPY;

    @Comment("Maximum allowed packet size in bytes")
    private int maxPacketSize = 67108864;

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment("Enable TLS encryption")
        private boolean enabled = true;
    }
}
```

생성되는 YAML:

```yaml
# TLS encryption settings
# TLS 암호화 설정
tls:
  # Enable TLS encryption
  enabled: true

# Compression algorithm (NONE, GZIP, SNAPPY, FAST_LZ)
compression: SNAPPY

# Maximum allowed packet size in bytes
max-packet-size: 67108864
```

---

## 5. 제약 조건 어노테이션

범위를 초과하는 값 입력 시 기본값으로 자동 복원됩니다.

```java
@Min(1)
@Comment("Maximum slots per arena")
private int maxSlots = 16;

@Max(100)
@Comment("Starting health percentage")
private int startHealth = 100;

@Constraint(Constraints.Positive.class)
@Comment("Respawn delay in seconds")
private double respawnDelay = 3.0;
```

### Map 관련 제약

```java
// 키 추가 차단, 값만 수정 가능
@MergeMap(restricted = true)
private Map<String, Integer> prices = mapOf("diamond", 100, "iron_ingot", 10);

// 키 정규화 후 파일에 재기록
@WriteKeyBack
private Map<String, Boolean> permissions = new LinkedHashMap<>();

// 디코딩 실패 시 예외 출력
@ThrowExceptions
private Map<String, RewardItem> rewards = new LinkedHashMap<>();
```

---

## 6. 특수 데이터 타입

| 타입 | 설명 | YAML 예시 |
|------|------|----------|
| `Duration` | 유저 친화적 시간 | `"30s"`, `"1h 30m"`, `"2d"` |
| `BooleanOrDefault` | 3상태 | `true` / `false` / `default` |
| `IntOr.Disabled` | 숫자 또는 비활성화 | `4` / `-1` |
| `DoubleOr.Default` | 실수 또는 기본값 | `1.5` / `default` |

```java
@Comment("Teleport cooldown (e.g. '30s', '1m 30s', '2h')")
private Duration teleportCooldown = Duration.of("30s");

@Comment("PVP setting (true/false/default)")
private BooleanOrDefault pvpEnabled = BooleanOrDefault.DEFAULT;

@Comment("Max party size (-1 = disabled)")
private IntOr.Disabled maxPartySize = IntOr.Disabled.of(4);
```

### 커스텀 직렬화기

| 직렬화기 | 설명 |
|----------|------|
| `MapSerializer` | 개별 항목 역직렬화 에러를 무시하고 로깅만 수행 |
| `ComponentSerializer` | Adventure Component ↔ MiniMessage 문자열 |
| `KeySerializer` | Adventure Key (네임스페이스) 자동 변환 |
| `SoundSerializer` | Adventure Sound ↔ 문자열/맵 객체 |
| `EnumValueSerializer` | 하이픈/공백 → 언더스코어 자동 치환 |
| `FastutilMapSerializer` | `Int2ObjectMap` 등 원시 타입 맵 |
| `FlattenedMapSerializer` | 다차원 맵 → 1차원 평탄화 |

---

## 7. 폴더 스캔 (ConfigList)

한 폴더에 여러 YAML 파일을 두고, 각각을 독립 설정 인스턴스로 로드합니다.

```java
// plugins/MyPlugin/arenas/*.yml 로드
ConfigList<ArenaConfig> arenas = registerConfigurations(ArenaConfig.class, ConfigPath.of("arenas"));

// 파일명(확장자 제외)으로 접근
ArenaConfig lobby = arenas.get("lobby");

// 전체 순회
arenas.forEach((name, config) -> {
    System.out.println("Arena: " + name + ", Slots: " + config.getMaxSlots());
});
```

---

## 8. 전체 예제

```java
public class MyPlugin extends RSPlugin {

    @Override
    protected void initialize() {
        registerConfiguration(MySettings.class, ConfigPath.of("config"));
        registerConfiguration(GameConfig.class, ConfigPath.of("Config/game"));
        registerConfigurations(ArenaConfig.class, ConfigPath.of("arenas"));
    }

    @Override
    protected void enable() {
        MySettings settings = getConfiguration(MySettings.class);
        if (settings.isVerbose()) {
            getLogger().info("Debug mode enabled");
        }
    }
}
```

---

## 9. API 레퍼런스

### RSPlugin (설정 관련)

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `registerConfiguration(class, path)` | `T` | 단일 YAML 등록 및 로드 |
| `registerConfigurations(class, path)` | `ConfigList<T>` | 폴더 내 모든 YAML 등록 |
| `getConfiguration(class)` | `T` | 등록된 설정 인스턴스 조회 |
| `getConfigurations(class)` | `ConfigList<T>` | 등록된 설정 목록 조회 |
| `reloadConfiguration(class)` | `boolean` | 설정 리로드 |

### 제약 어노테이션

| 어노테이션 | 대상 | 설명 |
|-----------|------|------|
| `@Min(value)` | 숫자 필드 | 최소값 강제 |
| `@Max(value)` | 숫자 필드 | 최대값 강제 |
| `@Constraint(Positive.class)` | 숫자 필드 | 양수만 허용 |
| `@MergeMap(restricted)` | Map 필드 | 키 추가 차단 |
| `@WriteKeyBack` | Map 필드 | 키 정규화 후 재기록 |
| `@ThrowExceptions` | Map 필드 | 디코딩 실패 예외 출력 |
