# RSFramework

모듈화된 Bukkit/Paper 플러그인 개발 프레임워크.

> **버전**: 4.3.2 · **Java**: 21 · **지원 서버**: 1.20.1+ (Spigot/Paper/Folia) · **라이선스**: GPL-3.0

---

## 프로젝트 구조

```
RSFramework/
├── LightDI/                    경량 DI 컨테이너
├── Configurate/                YAML 객체 매핑 래퍼
├── Storage/                    통합 스토리지 시스템
│   ├── Common/                 공통 API
│   ├── MySQL / MariaDB / PostgreSQL / MongoDB / SQLite / Json
├── Bridge/                     서버 간 통신 브로커
│   ├── Common/                 Bridge 인터페이스, BridgeChannel
│   ├── Redisson/               Redis 구현체
│   └── Proxium/                Netty 기반 자체 프록시 통신
│       ├── Common/API          공개 API
│       ├── Common/Core         내부 구현
│       ├── Bukkit / Bungee / Velocity
├── Platform/                   플랫폼 어댑터
│   ├── Spigot / Paper / Folia
│   ├── Bungee / Velocity
├── Framework/                  프레임워크 본체
│   ├── API/                    RSPlugin, RSCommand, RSListener
│   ├── Core/                   내부 구현
│   └── NMS/                    버전별 NMS 어댑터 (1.20 R1 ~ 1.21 R7)
└── docs/                       기술 문서
```

**빌드 산출물**: `./gradlew shadowJar` → `builds/plugin/RSFramework-{version}.jar`

---

## 공통 필드

`RSCommand`, `RSListener`, `RSInventory`는 동일한 `protected final` 필드를 제공합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `plugin` | `T` | 소유 플러그인 인스턴스 |
| `framework` | `Framework` | 프레임워크 코어 |
| `message` | `MessageTranslation` | 다국어 메시지 번역 |
| `command` | `CommandTranslation` | 다국어 명령어 번역 |
| `notifier` | `Notifier` | 메시지 전송 유틸리티 |

`RSCommand`는 추가로 `sender`, `player`, `audience` 필드를 제공합니다.

```java
// ✅ 필드에 직접 접근
plugin.reloadConfiguration(MyConfig.class);
notifier.announce(player, "완료!");
```

---

## 의존성

```kotlin
repositories {
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
}

dependencies {
    compileOnly("kr.rtustudio:framework-api:4.3.8")
}
```

---

## 시작하기

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

**라이프사이클**: `onLoad` → `initialize()` → `load()` → `onEnable` → `enable()` → `onDisable` → `disable()`

**자동 로깅**: 활성화/비활성화/리로드 시점에 콘솔 메시지가 자동 출력됩니다.

---

## 이벤트 리스너

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

## 명령어 시스템

계층형 구조, 권한 자동 등록, 쿨다운, 탭 자동완성을 지원합니다.

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

### 실행 결과 (Result)

| Result | 프레임워크 동작 |
|--------|---------------|
| `SUCCESS` / `FAILURE` | 없음 |
| `ONLY_PLAYER` / `ONLY_CONSOLE` | 자동 안내 메시지 |
| `NO_PERMISSION` | 자동 안내 메시지 |
| `NOT_FOUND_ONLINE_PLAYER` / `NOT_FOUND_OFFLINE_PLAYER` | 자동 안내 메시지 |
| `WRONG_USAGE` | 서브 명령어 목록 자동 표시 |

### 다국어 번역

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

## 설정 파일 관리

Configurate 기반 YAML 객체 매핑입니다.

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

상세 → [`docs/configuration.md`](docs/configuration.md)

---

## 다국어 지원

```java
String msg = message.get(player, "error.no-money");
notifier.announce(player, msg);
```

---

## 메시지 전송 (Notifier)

MiniMessage 포맷. 채팅, 액션바, 타이틀, 보스바, 크로스서버 브로드캐스트를 지원합니다.

```java
notifier.announce(player, "<aqua>아이템을 지급받았습니다!");    // 접두사 포함
notifier.send(player, "<yellow>경고 메시지");                  // 접두사 제외
notifier.title(player, "<bold><gold>레벨 업!", "<gray>새 스킬 해제");
Notifier.broadcastAll("<green>새로운 이벤트가 시작되었습니다!");
```

---

## 브릿지 통신

서버 간 **Pub/Sub** 브로드캐스트와 **RPC** 요청-응답을 지원합니다.

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

### 네트워크 플레이어 조회

```java
for (ProxyPlayer p : proxium.getPlayers().values()) {
    System.out.println(p.getName() + " → " + p.getServer());
}
```

### Redis — 분산 락

```java
Redis redis = getBridge(Redis.class);
redis.withLock("player-data-save", () -> { /* 안전한 저장 */ });
```

상세 → [`docs/bridge.md`](docs/bridge.md)

---

## 스토리지

JSON 문서 기반 통합 API로 모든 DB를 동일한 인터페이스로 관리합니다.

**지원**: JSON, SQLite, MySQL, MariaDB, PostgreSQL, MongoDB

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

상세 → [`docs/storage.md`](docs/storage.md)

---

## 스케줄러

### CraftScheduler (Bukkit/Paper/Folia)

```java
CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
}).delay(task -> {
    player.setHealth(1);
}, 20L);
```

### QuartzScheduler (Cron)

```java
QuartzScheduler.run("DailyReset", "0 0 0 * * ?", MyJob.class);
```

---

## 인벤토리 UI

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

## 커스텀 블록/아이템 통합

Nexo, Oraxen, ItemsAdder, MMOItems, EcoItems를 단일 API로 통합합니다.

```java
ItemStack sword = CustomItems.from("mmoitems:SWORD:FIRE_SWORD");
String id = CustomItems.to(player.getInventory().getItemInMainHand());
CustomBlocks.place(location, "oraxen:custom_ore");
```

---

## 빌드

```bash
./gradlew shadowJar          # 플러그인 JAR → builds/plugin/
./gradlew spotlessApply       # 코드 포맷팅
```

**요구사항**: JDK 21+, Gradle 9.3+
