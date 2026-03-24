# RSFramework

Modular Bukkit/Paper plugin development framework.

> **Version**: 4.5.5 · **Java**: 21 · **Supported Servers**: 1.20.1+ (Spigot/Paper/Folia) · **Proxy**: Velocity · **License**: GPL-3.0

---

## Project Structure

```
RSFramework/
├── LightDI/                    Lightweight DI container
├── Configurate/                YAML object mapping wrapper
├── Storage/                    Unified storage system
│   ├── Common/                 Common API
│   ├── MySQL / MariaDB / PostgreSQL / MongoDB / SQLite / Json
├── Bridge/                     Inter-server communication broker
│   ├── Common/                 Bridge interface, BridgeChannel
│   ├── Redisson/               Redis implementation
│   └── Proxium/                Netty-based direct proxy communication
│       ├── Common/API          Public API
│       ├── Common/Core         Internal implementation
│       ├── Bukkit / Velocity
├── Platform/                   Platform adapters
│   ├── Spigot / Paper / Folia
│   └── Velocity
├── Framework/                  Framework core
│   ├── API/                    RSPlugin, RSCommand, RSListener
│   ├── Core/                   Internal implementation
│   └── NMS/                    Version-specific NMS adapters (1.20 R1 ~ 1.21 R7)
└── docs/                       Technical documentation
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
    compileOnly("kr.rtustudio:framework-api:4.5.5")
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
        notifier.announce(event.getPlayer(), "<green>서버에 오신 것을 환영합니다!");
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
        notifier.announce("메인 명령어 실행됨!");
        return Result.SUCCESS;
    }

    @Override
    protected void reload(CommandArgs data) {
        plugin.reloadConfiguration(TestConfig.class);
    }
}
```

`registerCommand(cmd, true)` — `/{명령어} reload` 자동 추가.

### Execution Results

| Result | Framework Behavior |
|--------|-------------------|
| `SUCCESS` / `FAILURE` | None |
| `ONLY_PLAYER` / `ONLY_CONSOLE` | Auto info message |
| `NO_PERMISSION` | Auto info message |
| `NOT_FOUND_ONLINE_PLAYER` / `NOT_FOUND_OFFLINE_PLAYER` | Auto info message |
| `WRONG_USAGE` | Auto-show subcommands | |

### i18n Translation

`Translation/Command/{언어}.yml`:
```yaml
test:
  name: "테스트"
  description: "테스트 명령어 입니다"
  usage: "/테스트"
  commands:
    sub:
      name: "서브"
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
    private String welcomeMessage = "<green>환영합니다!";

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

```java
@Override
protected void initialize() {
    registerConfiguration(MyConfig.class, ConfigPath.of("Setting"));
    registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
}
```

Details → [`docs/configuration.md`](docs/configuration.md)

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
notifier.announce(player, "<aqua>아이템을 지급받았습니다!");    // 접두사 포함
notifier.send(player, "<yellow>경고 메시지");                  // 접두사 제외
notifier.title(player, "<bold><gold>레벨 업!", "<gray>새 스킬 해제");
Notifier.broadcastAll("<green>새로운 이벤트가 시작되었습니다!");
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

### Pub/Sub

```java
Proxium proxium = getBridge(Proxium.class);
BridgeChannel channel = BridgeChannel.of("myplugin", "shop");

// 타입별 구독 (여러 타입 개별 등록 가능)
proxium.subscribe(channel, BuyRequest.class, buy -> {
    getLogger().info(buy.playerName() + "님이 구매를 요청했습니다.");
});

// 발행
proxium.publish(channel, new BuyRequest("ipecter", "DIAMOND", 64));
```

### RPC

```java
// 응답 서버 (데이터 보유)
proxium.respond(channel)
    .on(BalanceRequest.class, (sender, req) -> {
        return new BalanceResponse(req.uuid(), getBalance(req.uuid()));
    })
    .error(e -> log.error("RPC 실패: " + e.getMessage()));

// 요청 서버 (데이터 필요)
proxium.request("Survival-1", channel, new BalanceRequest(uuid))
    .on(BalanceResponse.class, (sender, res) -> {
        player.sendMessage("잔고: " + res.balance());
    })
    .error(e -> player.sendMessage("요청 실패: " + e.type()));
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
redis.withLock("player-data-save", () -> { /* 안전한 저장 */ });
```

Details → [`docs/bridge.md`](docs/bridge.md)

---

## Storage

Unified JSON document-based API managing all databases with an identical interface.

**Supported**: JSON, SQLite, MySQL, MariaDB, PostgreSQL, MongoDB

```java
registerStorage("PlayerData", StorageType.MARIADB);
Storage storage = getStorage("PlayerData");

// 삽입
storage.add(JSON.of("uuid", uuid.toString()).append("name", "IPECTER").append("coins", 1000));

// 조회
storage.get(JSON.of("uuid", uuid.toString())).thenAccept(results -> {
    if (!results.isEmpty()) {
        int coins = results.get(0).get("coins").getAsInt();
    }
});

// 수정
storage.set(JSON.of("uuid", uuid.toString()), JSON.of("uuid", uuid.toString()).append("coins", 2000));
```

Details → [`docs/storage.md`](docs/storage.md)

---

## Scheduler

### CraftScheduler (Bukkit/Paper/Folia)

```java
// 비동기/동기 체이닝
CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
}).delay(task -> {
    player.setHealth(1);
}, 20L);

// 안전한 동기 결과 반환 (Folia Region 완벽 호환)
CraftScheduler.callSync(location, () -> {
    return location.getBlock().getType();
}).thenAccept(material -> {
    notifier.announce("해당 위치의 블록은: " + material);
});
```

Details → [`docs/scheduler.md`](docs/scheduler.md)

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
        Inventory inv = createInventory(27, ComponentFormatter.mini("내 인벤토리"));
        player.openInventory(inv);
    }

    @Override
    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        notifier.announce(event.player(), "슬롯 " + click.slot() + " 클릭됨!");
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
./gradlew shadowJar          # 플러그인 JAR → builds/plugin/
./gradlew spotlessApply       # 코드 포맷팅
```

**Requirements**: JDK 21+, Gradle 9.3+
