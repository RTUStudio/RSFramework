# RSFramework Configuration 시스템

RSFramework는 SpongePowered의 최신 `Configurate` 아키텍처를 근간으로 하여 극도로 안정적이고 고도화된 객체 지향형 설정 파일 매핑 시스템을 제공합니다. 개발자는 복잡한 `FileConfiguration`의 `get/set`에 얽매일 필요 없이, 자바의 필드(Field)와 YAML을 직접 매핑하는 안전하고 효율적인 설정 환경을 구축할 수 있습니다.

## 1. 프레임워크 아키텍처

```text
Configurate/                                   플랫폼 독립 모듈 (java-library)
└── kr.rtustudio.configurate.model
    ├── ConfigurationPart                      설정 객체 베이스 클래스 (모든 설정 클래스는 이를 상속)
    ├── Configuration                          YAML 로드·저장·리로드 처리 추상 레이어
    ├── ConfigPath                             설정 파일 및 폴더 경로를 정의하는 불변 레코드
    ├── ConfigList                             폴더 내 생성된 복수 YAML 파일의 안전 래퍼 (불변)
    ├── ConfigurationSerializer                내장 커스텀 직렬화기·제약 어노테이션 일괄 등록기
    ├── constraint/                            안전 장치 어노테이션 (@Constraint, @Min, @Max, Positive 등)
    ├── mapping/                               객체 매핑 헬퍼 (InnerClassFieldDiscoverer, MergeMap 등)
    ├── serializer/                            커스텀 파서 (ComponentSerializer, EnumValueSerializer 등)
    └── type/                                  안전 타입들 (Duration, BooleanOrDefault, IntOr 등)

Framework/API/
└── kr.rtustudio.framework.bukkit.api.configuration
    ├── RSConfiguration                        플러그인별 환경 설정 객체 인스턴스 중앙 메모리 레지스트리
    └── PluginConfiguration                    Configurate 노드와 Bukkit 플랫폼을 연결하는 래퍼
```

---

## 2. 기본 사용법

### 2.1. 설정 클래스 정의

모든 설정 클래스는 `ConfigurationPart`를 상속받고, Configurate가 필드를 인식하도록 합니다.

> [!IMPORTANT]
> **`@SuppressWarnings` 필수** — Configurate는 리플렉션으로 필드를 직접 조작하므로, IDE가 "사용되지 않음" 등의 경고를 발생시킵니다.
> 프레임워크의 모든 설정 클래스에서 아래 패턴을 사용합니다:
> ```java
> @SuppressWarnings({
>     "unused",              // 리플렉션 접근이므로 직접 참조 없음
>     "CanBeFinal",          // Configurate가 역직렬화 시 값을 주입
>     "FieldCanBeLocal",     // 필드가 YAML에 매핑되어야 함
>     "FieldMayBeFinal",     // 역직렬화 시 변경 가능해야 함
>     "InnerClassMayBeStatic" // 내부 클래스가 외부 인스턴스에 바인딩되어야 함
> })
> ```

#### 간단한 설정 클래스

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

### 2.2. 메인 플러그인에서 등록

```java
@Override
protected void initialize() {
    // 단일 설정 파일 → plugins/MyPlugin/config.yml
    MySettings settings = registerConfiguration(MySettings.class, ConfigPath.of("config"));
    
    // 폴더 내 모든 YAML → plugins/MyPlugin/classes/*.yml
    ConfigList<ClassConfig> classes = registerConfigurations(ClassConfig.class, ConfigPath.of("classes"));
}
```

### 2.3. 런타임 접근 및 리로드

```java
// 타입 캐스팅 없이 인스턴스 접근
MySettings settings = getConfiguration(MySettings.class);
int maxPlayers = settings.getMaxPlayers();

// 설정 리로드 (파일 변경 사항 반영)
reloadConfiguration(MySettings.class);
```

---

## 3. 내부 클래스를 활용한 계층 구조

YAML의 중첩 구조를 자바 내부 클래스(Inner Class)로 자연스럽게 매핑합니다.
`InnerClassFieldDiscoverer`가 리플렉션으로 자동 파싱하므로 `public static` 선언이 필요 없습니다.

### 실제 예제: ProxiumConfig (프레임워크 내부)

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
            TLS encryption settings for Proxium server-proxy connections
            Proxium 서버-프록시 연결의 TLS 암호화 설정""")
    public Tls tls;

    @Comment("""
            Compression algorithm for Proxium packets (NONE, GZIP, SNAPPY, FAST_LZ)
            Proxium 패킷 압축 알고리즘 (NONE, GZIP, SNAPPY, FAST_LZ)""")
    private CompressionType compression = CompressionType.SNAPPY;

    @Comment("""
            Maximum allowed packet size in bytes
            허용되는 최대 패킷 크기 (바이트)""")
    private int maxPacketSize = 67108864;

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment("""
                Enable TLS encryption for Proxium connections
                Proxium 연결에 TLS 암호화를 활성화합니다""")
        private boolean enabled = true;
    }
}
```

**생성되는 YAML:**

```yaml
# TLS encryption settings for Proxium server-proxy connections
# Proxium 서버-프록시 연결의 TLS 암호화 설정
tls:
  # Enable TLS encryption for Proxium connections
  # Proxium 연결에 TLS 암호화를 활성화합니다
  enabled: true

# Compression algorithm for Proxium packets (NONE, GZIP, SNAPPY, FAST_LZ)
# Proxium 패킷 압축 알고리즘 (NONE, GZIP, SNAPPY, FAST_LZ)
compression: SNAPPY

# Maximum allowed packet size in bytes
# 허용되는 최대 패킷 크기 (바이트)
max-packet-size: 67108864
```

### 실제 예제: RedisConfig (복잡한 다중 중첩)

```java
@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class RedisConfig extends ConfigurationPart {

    @Comment("""
            Enable the Redis bridge for cross-server data sharing
            서버 간 데이터 공유를 위한 Redis 연결을 활성화합니다""")
    public boolean enabled = false;

    @Comment("Redis server connection settings")
    public Connection connection;

    @Comment("TLS encryption settings for secure Redis connections")
    public Tls tls;

    @Comment("Redis Sentinel settings for high availability")
    public Sentinel sentinel;

    @Comment("Redis Cluster settings for horizontal scaling")
    public Cluster cluster;

    @Comment("Distributed lock settings for concurrent access control")
    public Lock lock;

    // ─── 내부 클래스들 ───

    @Getter
    public class Connection extends ConfigurationPart {
        @Comment("Redis server hostname or IP address")
        private String host = "127.0.0.1";

        @Comment("Redis server port")
        private int port = 6379;

        @Comment("Redis authentication password (leave empty for no auth)")
        private String password = "";

        @Comment("Redis database index (0-15)")
        private int database = 0;
    }

    @Getter
    public class Tls extends ConfigurationPart {
        @Comment("Enable TLS encryption for Redis connections")
        private boolean enabled = false;
    }

    @Getter
    public class Sentinel extends ConfigurationPart {
        private boolean enabled = false;
        private String masterName = "mymaster";
        private String[] addresses = {"redis://127.0.0.1:26379"};
    }

    @Getter
    public class Cluster extends ConfigurationPart {
        private boolean enabled = false;
        private String[] addresses = {"redis://127.0.0.1:7000"};
    }

    @Getter
    public class Lock extends ConfigurationPart {
        @Comment("Maximum time to wait for lock acquisition (ms)")
        private long waitTime = 3000;

        @Comment("Maximum time to hold the lock before auto-release (ms)")
        private long leaseTime = 5000;
    }
}
```

### 실제 예제: ThemeModule (UI 테마 설정)

```java
@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class ThemeModule extends ConfigurationPart {

    public Gradient gradient;

    @Comment("Prefix character wrapping the plugin name in messages")
    private String prefix = "『";

    @Comment("Suffix character wrapping the plugin name in messages")
    private String suffix = "』";

    @Comment("Hover tooltip displayed when hovering over system messages (MiniMessage format)")
    private String systemMessage =
            "<gradient:#2979FF:#7C4DFF>시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

    @Getter
    public class Gradient extends ConfigurationPart {
        @Comment("Start color of the plugin name gradient (hex)")
        private String start = "#2979FF";

        @Comment("End color of the plugin name gradient (hex)")
        private String end = "#7C4DFF";
    }
}
```

---

## 4. 어노테이션 기반 제약 조건

### 숫자 범위 제약

사용자가 YAML에 범위를 초과하는 값을 입력하면, `SerializationException`이 발생하고 **기본값으로 자동 복원**됩니다.

```java
import kr.rtustudio.configurate.model.constraint.Constraints.Min;
import kr.rtustudio.configurate.model.constraint.Constraints.Max;
import kr.rtustudio.configurate.model.constraint.Constraint;
import kr.rtustudio.configurate.model.constraint.Constraints;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal"})
public class GameConfig extends ConfigurationPart {

    // 최소 1 이상
    @Min(1)
    @Comment("Maximum slots per arena")
    private int maxSlots = 16;

    // 최대 100 이하
    @Max(100)
    @Comment("Starting health percentage")
    private int startHealth = 100;

    // 커스텀 제약: 양수만 허용
    @Constraint(Constraints.Positive.class)
    @Comment("Respawn delay in seconds")
    private double respawnDelay = 3.0;
}
```

### Map 관련 제약

```java
import kr.rtustudio.configurate.model.serializer.collection.map.MergeMap;
import kr.rtustudio.configurate.model.serializer.collection.map.WriteKeyBack;
import kr.rtustudio.configurate.model.serializer.collection.map.ThrowExceptions;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal"})
public class ShopConfig extends ConfigurationPart {

    // restricted=true: 사용자가 새 키 추가 불가, 값만 수정 가능
    @MergeMap(restricted = true)
    @Comment("Item prices (key modification restricted)")
    private Map<String, Integer> prices = Map.of(
        "diamond", 100,
        "iron_ingot", 10
    );

    // 키 정규화: 잘못된 대소문자를 자동 복원
    @WriteKeyBack
    @Comment("Permission groups")
    private Map<String, Boolean> permissions = new LinkedHashMap<>();

    // 디코딩 실패 시 예외를 콘솔에 출력
    @ThrowExceptions
    @Comment("Reward items")
    private Map<String, RewardItem> rewards = new LinkedHashMap<>();
}
```

---

## 5. 내장 커스텀 직렬화기 및 특별 데이터 타입

| 직렬화기 및 타입 | 설명 |
|------------------|------|
| `ComponentSerializer` | Kyori Adventure Component를 MiniMessage(`<red>` 등) 문자열로 양방향 직렬화 |
| `EnumValueSerializer` | Enum 작성 시 띄어쓰기·하이픈(`-`)을 자바 언더스코어(`_`)로 자동 치환 |
| `FastutilMapSerializer` | `Int2ObjectMap`, `Long2ObjectMap` 등 원시 타입 맵 직렬화 |
| `FlattenedMapSerializer` | 다차원 맵을 1차원 평탄화 YAML로 내보내기 |
| `Duration` 타입 | `"30s"`, `"1h 30m"`, `"2d"` → Java `Duration` 자동 변환 |
| `*Or` 래퍼 타입 | `BooleanOrDefault`, `IntOr.Disabled`, `DoubleOr.Default` 등 특수값 처리 |

### Duration 사용 예제

```java
import kr.rtustudio.configurate.model.type.Duration;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal"})
public class CooldownConfig extends ConfigurationPart {

    @Comment("Teleport cooldown (e.g. '30s', '1m 30s', '2h')")
    private Duration teleportCooldown = Duration.of("30s");

    @Comment("Combat tag duration")
    private Duration combatTag = Duration.of("15s");
}
```

생성되는 YAML:
```yaml
# Teleport cooldown (e.g. '30s', '1m 30s', '2h')
teleport-cooldown: 30s

# Combat tag duration
combat-tag: 15s
```

### IntOr / BooleanOrDefault 사용 예제

```java
import kr.rtustudio.configurate.model.type.BooleanOrDefault;
import kr.rtustudio.configurate.model.type.number.IntOr;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal"})
public class FeatureConfig extends ConfigurationPart {

    // "default" 또는 true/false 입력 가능
    @Comment("Use server default PVP setting or override (true/false/default)")
    private BooleanOrDefault pvpEnabled = BooleanOrDefault.DEFAULT;

    // -1 입력 시 "비활성화"로 해석
    @Comment("Max party size (-1 = disabled)")
    private IntOr.Disabled maxPartySize = IntOr.Disabled.of(4);
}
```

---

## 6. 폴더 스캔 (ConfigList)

한 폴더에 여러 YAML 파일을 두고, 각각을 독립 설정 인스턴스로 로드합니다.

```java
// plugins/MyPlugin/arenas/*.yml 로드
ConfigList<ArenaConfig> arenas = registerConfigurations(ArenaConfig.class, ConfigPath.of("arenas"));

// 파일명(확장자 제외)으로 접근
ArenaConfig lobby = arenas.get("lobby");
ArenaConfig pvpArena = arenas.get("pvp_arena");

// 전체 순회
arenas.forEach((name, config) -> {
    System.out.println("Arena: " + name + ", Slots: " + config.getMaxSlots());
});
```

---

## 7. 플러그인 전체 구성 예제

```java
public class MyPlugin extends RSPlugin {

    @Override
    protected void initialize() {
        // 메인 설정
        registerConfiguration(MySettings.class, ConfigPath.of("config"));
        
        // 게임 설정
        registerConfiguration(GameConfig.class, ConfigPath.of("Config/game"));
        
        // 쿨다운 설정
        registerConfiguration(CooldownConfig.class, ConfigPath.of("Config/cooldowns"));
        
        // 다중 아레나 설정 (폴더 스캔)
        registerConfigurations(ArenaConfig.class, ConfigPath.of("arenas"));
    }

    @Override
    protected void enable() {
        MySettings settings = getConfiguration(MySettings.class);
        if (settings.isVerbose()) {
            getLogger().info("Debug mode enabled");
        }
        
        // 게임 로직에서 사용
        GameConfig game = getConfiguration(GameConfig.class);
        getLogger().info("Max slots: " + game.getMaxSlots());
    }
}
```

---

## 8. API 레퍼런스

### RSPlugin (설정 관련)

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `registerConfiguration(class, path)` | `T` | 단일 YAML 등록 및 로드 |
| `registerConfiguration(class, path, serializers)` | `T` | 커스텀 직렬화 포함 |
| `registerConfigurations(class, path)` | `ConfigList<T>` | 폴더 내 모든 YAML 등록 |
| `registerConfigurations(class, path, serializers)` | `ConfigList<T>` | 커스텀 직렬화 포함 |
| `getConfiguration(class)` | `T` | 등록된 설정 인스턴스 조회 |
| `getConfigurations(class)` | `ConfigList<T>` | 등록된 설정 목록 조회 |
| `reloadConfiguration(class)` | `boolean` | 설정 리로드 |

### 제약 어노테이션

| 어노테이션 | 대상 | 설명 |
|-----------|------|------|
| `@Min(value)` | 숫자 필드 | 최소값 이상 강제 |
| `@Max(value)` | 숫자 필드 | 최대값 이하 강제 |
| `@Constraint(Constraints.Positive.class)` | 숫자 필드 | 양수만 허용 |
| `@MergeMap(restricted=true)` | Map 필드 | 키 추가 차단, 값만 수정 가능 |
| `@WriteKeyBack` | Map 필드 | 키 정규화 후 파일에 재기록 |
| `@ThrowExceptions` | Map 필드 | 디코딩 실패 시 예외 출력 |

### 특수 타입

| 타입 | 설명 |
|------|------|
| `Duration` | 유저 친화적 시간 문자열 (`"30s"`, `"1h 30m"`, `"2d"`) |
| `BooleanOrDefault` | `true` / `false` / `default` 3상태 |
| `IntOr.Disabled` | 숫자 또는 비활성화(`-1`) |
| `DoubleOr.Default` | 실수 또는 기본값 |
