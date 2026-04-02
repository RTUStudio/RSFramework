# RSFramework

Modular Bukkit/Paper plugin development framework.

> **Version**: 4.7.12 · **Java**: 21 · **Supported Servers**: 1.20.1+ (Spigot/Paper/Folia) · **Proxy**: Velocity · **License**: GPL-3.0

**[🇰🇷 한국어 문서](docs/kr/)**

---

## Project Structure

```text
RSFramework/
├── Bridge/                     Inter-server communication broker
│   ├── Common/                 Bridge interface, BridgeChannel
│   ├── Proxium/                Netty-based direct proxy communication
│   │   ├── Common/             Public API & Internal implementation
│   │   ├── Bukkit/             Bukkit-side implementation
│   │   └── Velocity/           Velocity-side implementation
│   └── Redisson/               Redis implementation
├── Configurate/                YAML object mapping wrapper
├── Framework/                  Framework core
│   ├── API/                    RSPlugin, RSCommand, RSListener
│   ├── Core/                   Internal implementation
│   └── NMS/                    Version-specific NMS adapters (1.20 R1 ~ 1.21 R7)
├── LightDI/                    Lightweight DI container
├── Platform/                   Platform adapters
│   ├── Folia/
│   ├── Paper/
│   ├── Spigot/
│   └── Velocity/
├── Storage/                    Unified storage system
│   ├── Common/                 Common API
│   ├── Json/
│   ├── MariaDB/
│   ├── MongoDB/
│   ├── MySQL/
│   ├── PostgreSQL/
│   └── SQLite/
└── docs/                       Technical documentation
    ├── en/                     English documentation
    └── kr/                     Korean documentation
```

**Build output**: `./gradlew shadowJar` → `builds/plugin/RSFramework-{version}.jar`

---

## Common Fields

`RSCommand`, `RSListener`, `RSInventory` share identical `protected final` fields.

| Field | Type | Description |
|-------|------|-------------|
| `plugin` | `T` | Owning plugin instance |
| `framework` | `Framework` | Framework core |
| `message` | `MessageTranslation` | i18n message translation |
| `command` | `CommandTranslation` | i18n command translation |
| `notifier` | `Notifier` | Message sending utility |

`RSCommand` additionally provides `sender`, `player`, `audience` fields.

```java
// ✅ Direct field access
plugin.reloadConfiguration(MyConfig.class);
notifier.announce(player, "Done!");
```

---

## Dependency

```kotlin
repositories {
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
}

dependencies {
    compileOnly("kr.rtustudio:framework-api:4.7.12")
}
```

---

## Getting Started

```java
import kr.rtustudio.framework.bukkit.api.RSPlugin;

public class MyPlugin extends RSPlugin {

    @Override
    protected void enable() {
        registerConfiguration(PerkConfig.class, ConfigPath.of("Perk"));
        registerCommand(new MainCommand(this), true);
        registerEvent(new PlayerAttack(this));
    }

    @Override
    protected void disable() { }
}
```

**Lifecycle**: `onLoad` → `initialize()` → `load()` → `onEnable` → `enable()` → `onDisable` → `disable()`

**Auto-logging**: Console messages are automatically printed on enable/disable/reload.

---

## Event Listeners

```java
public class JoinListener extends RSListener<MyPlugin> {

    public JoinListener(MyPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        notifier.announce(event.getPlayer(), "<green>Welcome to the server!");
    }
}
```

---

## Command System

Supports hierarchical structure, auto-permission registration, cooldowns, and tab completion.

```java
public class MainCommand extends RSCommand<MyPlugin> {

    public MainCommand(MyPlugin plugin) {
        super(plugin, "test", PermissionDefault.OP);
        registerCommand(new SubCommand(plugin));
    }

    @Override
    protected Result execute(CommandArgs data) {
        notifier.announce("Main command executed!");
        return Result.SUCCESS;
    }

    @Override
    protected void reload(CommandArgs data) {
        plugin.reloadConfiguration(TestConfig.class);
    }
}
```

`registerCommand(cmd, true)` — automatically adds `/{command} reload`.

### Execution Results

| Result | Framework Behavior |
|--------|-------------------|
| `SUCCESS` / `FAILURE` | None |
| `ONLY_PLAYER` / `ONLY_CONSOLE` | Auto info message |
| `NO_PERMISSION` | Auto info message |
| `NOT_FOUND_ONLINE_PLAYER` / `NOT_FOUND_OFFLINE_PLAYER` | Auto info message |
| `WRONG_USAGE` | Auto-show subcommands |

### i18n Translation

`Translation/Command/{language}.yml`:
```yaml
test:
  name: "test"
  description: "A test command"
  usage: "/test"
  commands:
    sub:
      name: "sub"
```

---

## Configuration Management

Configurate-based YAML object mapping.

```java
@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class MyConfig extends ConfigurationPart {

    @Comment("Welcome message")
    private String welcomeMessage = "<green>Welcome!";

    @Min(1)
    @Comment("Maximum players")
    private int maxPlayers = 100;

    public Connection connection;

    @Getter
    public class Connection extends ConfigurationPart {
        private String host = "127.0.0.1";
        private int port = 25565;
    }
}
```

### Reload-safe Collection Helpers

`ConfigurationPart` provides mutable collection factories that are safe during configuration reload.

> **Note**: Using immutable collections like `List.of()`, `Map.of()` as config defaults will cause `UnsupportedOperationException` on reload. Always use `listOf()`, `mapOf()` helpers.

```java
public class WhitelistConfig extends ConfigurationPart {

    // varargs style
    private List<String> commands = listOf("help", "spawn");

    // Consumer style (complex initialization)
    private Map<String, List<String>> groups = mapOf(map -> {
        map.put("default", listOf("help"));
        map.put("worldedit", listOf("/wand", "/copy"));
    });

    // key-value style
    private Map<String, String> aliases = mapOf("h", "help", "s", "spawn");
}
```

### Registration

```java
@Override
protected void initialize() {
    registerConfiguration(MyConfig.class, ConfigPath.of("Setting"));
    registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
}
```

Details → [`docs/en/configuration.md`](docs/en/configuration.md)

---

## i18n Support

```java
String msg = message.get(player, "error.no-money");
notifier.announce(player, msg);
```

---

## Messaging (Notifier)

MiniMessage format. Supports chat, action bar, title, boss bar, and cross-server broadcast.

```java
notifier.announce(player, "<aqua>Item received!");        // with prefix
notifier.send(player, "<yellow>Warning message");         // without prefix
notifier.title(player, "<bold><gold>Level Up!", "<gray>New skill unlocked");
Notifier.broadcastAll("<green>A new event has started!");
```

---

## Bridge Communication

Supports inter-server **Pub/Sub** broadcast and **RPC** request-response.

```
Bridge (isConnected + close)
├── Broadcast (register · subscribe · publish · unsubscribe)
├── Transaction (request · respond · getRequestTimeout)
│
├── Redis extends Broadcast
└── Proxium extends Broadcast, Transaction
```

### Architecture

| Layer | Role |
|-------|------|
| **Proxium** (Bridge) | Pure communication infrastructure — packet serialization, Netty transport, TLS |
| **RSFramework Bukkit** | Application logic — teleport execution, message handling |
| **RSFramework Velocity** | Teleport routing — server transfer coordination |

### Pub/Sub

```java
Proxium proxium = getBridge(Proxium.class);
BridgeChannel channel = BridgeChannel.of("myplugin", "shop");

// Type-specific subscription (multiple types can be registered individually)
proxium.subscribe(channel, BuyRequest.class, buy -> {
    getLogger().info(buy.playerName() + " requested a purchase.");
});

// Publish
proxium.publish(channel, new BuyRequest("ipecter", "DIAMOND", 64));
```

### RPC

```java
// Response server (data holder)
proxium.respond(channel)
    .on(BalanceRequest.class, (sender, req) -> {
        return new BalanceResponse(req.uuid(), getBalance(req.uuid()));
    })
    .error(e -> log.error("RPC failed: " + e.getMessage()));

// Request server (needs data)
proxium.request("Survival-1", channel, new BalanceRequest(uuid))
    .on(BalanceResponse.class, (sender, res) -> {
        player.sendMessage("Balance: " + res.balance());
    })
    .error(e -> player.sendMessage("Request failed: " + e.type()));
```

### Network Player Query

```java
for (ProxyPlayer p : proxium.getPlayers().values()) {
    System.out.println(p.getName() + " → " + p.getServer());
}
```

### Redis — Distributed Locking

```java
Redis redis = getBridge(Redis.class);
redis.withLock("player-data-save", () -> { /* safe save */ });
```

Details → [`docs/en/bridge.md`](docs/en/bridge.md)

---

## Storage

Unified JSON document-based API managing all databases with an identical interface.

**Supported**: JSON, SQLite, MySQL, MariaDB, PostgreSQL, MongoDB

```java
registerStorage("PlayerData", StorageType.MARIADB);
Storage storage = getStorage("PlayerData");

// Insert
storage.add(JSON.of("uuid", uuid.toString()).append("name", "IPECTER").append("coins", 1000));

// Query
storage.get(JSON.of("uuid", uuid.toString())).thenAccept(results -> {
    if (!results.isEmpty()) {
        int coins = results.get(0).get("coins").getAsInt();
    }
});

// Update
storage.set(JSON.of("uuid", uuid.toString()), JSON.of("uuid", uuid.toString()).append("coins", 2000));
```

Details → [`docs/en/storage.md`](docs/en/storage.md)

---

## Scheduler

### CraftScheduler (Bukkit/Paper/Folia)

```java
// Entity-based sync execution (Folia Region compatible)
CraftScheduler.sync(player, () -> {
    player.teleport(location);
});

// Async/sync chaining
CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
}).delay(task -> {
    player.setHealth(1);
}, 20L);

// Safe sync result return
CraftScheduler.callSync(location, () -> {
    return location.getBlock().getType();
}).thenAccept(material -> {
    notifier.announce("Block at location: " + material);
});
```

Details → [`docs/en/scheduler.md`](docs/en/scheduler.md)

### QuartzScheduler (Cron)

```java
QuartzScheduler.run("DailyReset", "0 0 0 * * ?", MyJob.class);
```

---

## Inventory UI

```java
public class MyGUI extends RSInventory<MyPlugin> {

    public MyGUI(MyPlugin plugin) {
        super(plugin);
    }

    public void open(Player player) {
        Inventory inv = createInventory(27, ComponentFormatter.mini("My Inventory"));
        player.openInventory(inv);
    }

    @Override
    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        notifier.announce(event.player(), "Slot " + click.slot() + " clicked!");
        return true;
    }
}
```

---

## Custom Block/Item Integration

Unifies Nexo, Oraxen, ItemsAdder, MMOItems, EcoItems under a single API.

```java
ItemStack sword = CustomItems.from("mmoitems:SWORD:FIRE_SWORD");
String id = CustomItems.to(player.getInventory().getItemInMainHand());
CustomBlocks.place(location, "oraxen:custom_ore");
```

---

## Build

```bash
./gradlew shadowJar          # Plugin JAR → builds/plugin/
./gradlew spotlessApply       # Code formatting
```

**Requirements**: JDK 21+, Gradle 9.3+
