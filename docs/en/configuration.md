# RSFramework Configuration System

Object-oriented YAML configuration mapping system. Based on SpongePowered Configurate, it directly maps Java class fields to YAML.

---

## 1. Architecture

```text
Configurate/                                   Platform-independent module
└── kr.rtustudio.configurate.model
    ├── ConfigurationPart                      Configuration object base class
    ├── Configuration                          YAML load/save/reload handler
    ├── ConfigPath                             Configuration file path definition
    ├── ConfigList                             Multiple YAML file wrapper
    ├── constraint/                            Constraint annotations (@Min, @Max, etc.)
    ├── mapping/                               Object mapping helpers
    ├── serializer/                            Custom serializers
    └── type/                                  Special types (Duration, BooleanOrDefault, etc.)
```

---

## 2. Basic Usage

### 2.1. Defining a Configuration Class

All configuration classes extend `ConfigurationPart`.

> [!IMPORTANT]
> **`@SuppressWarnings` required** — Configurate manipulates fields via reflection.
> ```java
> @SuppressWarnings({
>     "unused",              // Accessed via reflection
>     "CanBeFinal",          // Values injected during deserialization
>     "FieldCanBeLocal",     // Fields must be mapped to YAML
>     "FieldMayBeFinal",     // Must be modifiable during deserialization
>     "InnerClassMayBeStatic" // Inner classes must bind to outer instance
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
    private String welcomeMessage = "<green>Welcome to the server!</green>";

    @Comment("Enable debug logging")
    private boolean verbose = false;
}
```

### 2.2. Registration

```java
@Override
protected void initialize() {
    // Single config file → plugins/MyPlugin/Config/config.yml
    registerConfiguration(MySettings.class, ConfigPath.of("config"));
    
    // Folder scan → plugins/MyPlugin/Config/classes/*.yml
    registerConfigurations(ClassConfig.class, ConfigPath.of("classes"));
}
```

### 2.3. Runtime Access & Reload

```java
MySettings settings = getConfiguration(MySettings.class);
int maxPlayers = settings.getMaxPlayers();

// Reload
reloadConfiguration(MySettings.class);
```

---

## 3. Reload Safety

During configuration reload, Configurate's `ObjectMapper.Mutable.load()` reuses existing collection objects by calling `clear()` → `addAll()` / `putAll()`. Therefore, using **immutable collections** such as `List.of()` or `Map.of()` as default values will cause `UnsupportedOperationException` on reload.

`ConfigurationPart` provides helper methods that create mutable collections to prevent this.

### 3.1. `listOf()` — Mutable List

```java
// varargs: simple initialization
private List<String> commands = listOf("help", "spawn", "home");

// Consumer: complex initialization
private List<String> rewards = listOf(list -> {
    list.add("diamond:10");
    list.add("gold_ingot:64");
});
```

### 3.2. `mapOf()` — Mutable Map

```java
// key-value style (0–5 entries)
private Map<String, Integer> prices = mapOf("diamond", 100, "iron_ingot", 10);

// Consumer style: complex initialization
private Map<String, List<String>> groups = mapOf(map -> {
    map.put("default", listOf("help"));
    map.put("admin", listOf("help", "ban", "kick"));
});

// Empty map
private Map<String, String> aliases = mapOf();
```

### 3.3. `make()` — Generic Initialization

Applies initialization logic to any object after creation.

```java
private Set<String> blockedWorlds = make(new HashSet<>(), set -> {
    set.add("lobby");
    set.add("hub");
});
```

> [!WARNING]
> Do NOT use `List.of()`, `Map.of()`, `Set.of()`, or `Collections.unmodifiableList()` as configuration field defaults. They will cause server errors on reload.

---

## 4. Hierarchical Structure (Inner Classes)

Map YAML's nested structure to Java inner classes.

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
            TLS encryption settings""")
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

Generated YAML:

```yaml
# TLS encryption settings
tls:
  # Enable TLS encryption
  enabled: true

# Compression algorithm (NONE, GZIP, SNAPPY, FAST_LZ)
compression: SNAPPY

# Maximum allowed packet size in bytes
max-packet-size: 67108864
```

---

## 5. Constraint Annotations

Values exceeding the range are automatically restored to their defaults.

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

### Map Constraints

```java
// Block new keys, only allow value modifications
@MergeMap(restricted = true)
private Map<String, Integer> prices = mapOf("diamond", 100, "iron_ingot", 10);

// Normalize keys and write back to file
@WriteKeyBack
private Map<String, Boolean> permissions = new LinkedHashMap<>();

// Throw exceptions on decoding failure
@ThrowExceptions
private Map<String, RewardItem> rewards = new LinkedHashMap<>();
```

---

## 6. Special Data Types

| Type | Description | YAML Example |
|------|-------------|-------------|
| `Duration` | User-friendly time | `"30s"`, `"1h 30m"`, `"2d"` |
| `BooleanOrDefault` | Tri-state | `true` / `false` / `default` |
| `IntOr.Disabled` | Number or disabled | `4` / `-1` |
| `DoubleOr.Default` | Double or default | `1.5` / `default` |

```java
@Comment("Teleport cooldown (e.g. '30s', '1m 30s', '2h')")
private Duration teleportCooldown = Duration.of("30s");

@Comment("PVP setting (true/false/default)")
private BooleanOrDefault pvpEnabled = BooleanOrDefault.DEFAULT;

@Comment("Max party size (-1 = disabled)")
private IntOr.Disabled maxPartySize = IntOr.Disabled.of(4);
```

### Custom Serializers

| Serializer | Description |
|------------|-------------|
| `MapSerializer` | Ignores individual map entry deserialization errors (logs only) |
| `ComponentSerializer` | Adventure Component ↔ MiniMessage string |
| `KeySerializer` | Adventure Key (namespace) auto-conversion |
| `SoundSerializer` | Adventure Sound ↔ string/map object |
| `EnumValueSerializer` | Auto-convert hyphens/spaces → underscores |
| `FastutilMapSerializer` | Primitive type maps like `Int2ObjectMap` |
| `FlattenedMapSerializer` | Multi-dimensional map → 1D flattening |

---

## 7. Folder Scan (ConfigList)

Place multiple YAML files in a folder and load each as an independent configuration instance.

```java
// Load plugins/MyPlugin/Config/arenas/*.yml
ConfigList<ArenaConfig> arenas = registerConfigurations(ArenaConfig.class, ConfigPath.of("arenas"));

// Access by filename (without extension)
ArenaConfig lobby = arenas.get("lobby");

// Iterate all
arenas.forEach((name, config) -> {
    System.out.println("Arena: " + name + ", Slots: " + config.getMaxSlots());
});
```

---

## 8. Full Example

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

## 9. API Reference

### RSPlugin (Configuration)

| Method | Returns | Description |
|--------|---------|-------------|
| `registerConfiguration(class, path)` | `T` | Register and load a single YAML |
| `registerConfigurations(class, path)` | `ConfigList<T>` | Register all YAML files in a folder |
| `getConfiguration(class)` | `T` | Get registered config instance |
| `getConfigurations(class)` | `ConfigList<T>` | Get registered config list |
| `reloadConfiguration(class)` | `boolean` | Reload configuration |

### Constraint Annotations

| Annotation | Target | Description |
|-----------|--------|-------------|
| `@Min(value)` | Numeric fields | Enforce minimum value |
| `@Max(value)` | Numeric fields | Enforce maximum value |
| `@Constraint(Positive.class)` | Numeric fields | Allow only positive values |
| `@MergeMap(restricted)` | Map fields | Block new key additions |
| `@WriteKeyBack` | Map fields | Normalize keys and rewrite |
| `@ThrowExceptions` | Map fields | Throw exceptions on decode failure |
