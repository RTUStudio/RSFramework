# RSFramework

모듈화된 Bukkit/Paper 플러그인 개발 프레임워크.

> **버전**: 4.3.0 · **Java**: 21 · **지원 서버**: 1.20.1+ (Spigot/Paper/Folia) · **라이선스**: GPL-3.0

---

## 프로젝트 구조

```
RSFramework/
├── LightDI/                    경량 DI 컨테이너 (kr.rtustudio.cdi)
├── Configurate/                YAML 객체 매핑 래퍼 (kr.rtustudio.configurate.model)
├── Storage/                    통합 스토리지 시스템
│   ├── Common/                 공통 API (Storage, JSON, StorageType)
│   ├── MySQL / MariaDB / PostgreSQL / MongoDB / SQLite / Json
├── Bridge/                     서버 간 통신 브로커
│   ├── Common/                 Bridge 인터페이스, BridgeChannel
│   ├── Redisson/               Redis 구현체
│   └── Proxium/                Netty 기반 자체 프록시 통신
│       ├── Common/API          Proxium 공개 API
│       ├── Common/Core         ProxiumProxy, ProxiumServer
│       ├── Bukkit / Bungee / Velocity
├── Platform/                   플랫폼 어댑터
│   ├── Spigot / Paper / Folia  Bukkit 계열
│   ├── Bungee / Velocity       프록시 계열
├── Framework/                  프레임워크 본체
│   ├── API/                    RSPlugin, RSCommand, RSListener 등 공개 API
│   ├── Core/                   내부 구현
│   └── NMS/                    버전별 NMS 어댑터 (1.20 R1 ~ 1.21 R7)
└── docs/                       기술 문서 (bridge, configuration, storage)
```

**빌드 산출물**: 루트 `shadowJar` 태스크가 모든 모듈을 하나의 플러그인 JAR로 합칩니다. (`builds/plugin/RSFramework-{version}.jar`)

---

## 공통 제공 필드 (`protected final`)

`RSCommand`, `RSListener`, `RSInventory`는 모두 동일한 공통 필드를 `protected final`로 제공합니다. `getPlugin()`, `getFramework()` 같은 Getter 없이 **필드에 직접 접근**하여 사용합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `plugin` | `T` (플러그인 타입) | 소유 플러그인 인스턴스 |
| `framework` | `Framework` | 프레임워크 코어 |
| `message` | `MessageTranslation` | 다국어 메시지 번역 |
| `command` | `CommandTranslation` | 다국어 명령어 번역 |
| `notifier` | `Notifier` | 메시지 전송 유틸리티 |

`RSCommand`는 추가로 다음 필드를 제공합니다.

| 필드         | 타입              | 설명                                            |
|------------|-----------------|-----------------------------------------------|
| `sender`   | `CommandSender` | 명령어 실행자 (getter 접근)                           |
| `player`   | `Player`        | 명령어 실행 플레이어 (플레이어가 아닌 경우 null 반환) |
| `audience` | `Audience`      | Adventure Audience (getter 접근)                |

```java
// ✅ 올바른 사용
plugin.reloadConfiguration(MyConfig.class);
notifier.announce(player, "완료!");

// ❌ 불필요한 getter 호출
getPlugin().reloadConfiguration(MyConfig.class);
```

---

## 시작하기

`RSPlugin`을 상속받아 메인 클래스를 작성합니다.

```java
import kr.rtustudio.framework.bukkit.api.RSPlugin;

public class MyPlugin extends RSPlugin {

    @Override
    protected void enable() {
        // 명령어·리스너·설정 등록
    }

    @Override
    protected void disable() { }
}
```

`RSPlugin`은 `onLoad` → `initialize()` → `load()` → `onEnable` → `enable()` → `onDisable` → `disable()` 순서로 라이프사이클을 제공합니다.

### 자동 로깅

프레임워크가 플러그인의 **활성화, 비활성화, 리로드** 시점에 콘솔 메시지를 자동으로 출력합니다. 개발자가 직접 로깅 코드를 작성할 필요가 없습니다.

```java
// ✅ 올바른 예시 — 로깅 없이 깔끔하게
@Override
protected void enable() {
    registerConfiguration(PerkConfig.class, ConfigPath.of("Perk"));
    registerCommand(new MainCommand(this), true);
    registerEvent(new PlayerAttack(this));
}

@Override
protected void disable() {
    if (perkModule != null) {
        perkModule.close();
    }
}
```

```java
// ❌ 잘못된 예시 — 프레임워크가 이미 출력하므로 중복됨
@Override
protected void enable() {
    // ...
    getLogger().info("MyPlugin Enabled!");    // 불필요
}
```

---

## 이벤트 리스너 (RSListener)

`RSListener<T>`를 상속하면 DI를 통해 이벤트가 자동으로 등록됩니다.

```java
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

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

## 명령어 시스템 (RSCommand)

계층형 구조, 권한 자동 등록, 쿨다운, 탭 자동완성을 지원합니다.

```java
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.permissions.PermissionDefault;

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
        plugin.reloadConfigurations(ListConfig.class);
    }
}

public class SubCommand extends RSCommand<MyPlugin> {

    public SubCommand(MyPlugin plugin) {
        super(plugin, "sub", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        notifier.announce("서브 명령어 실행됨!");
        return Result.SUCCESS;
    }
}
```

`enable()`에서 등록 시 `true`를 전달하면 `/{명령어} reload` 서브 명령어가 자동 추가됩니다.
이 자동 생성된 `reload` 명령어는 프레임워크가 자체적으로 처리하여 완료 메시지까지 출력하므로 **별도의 번역 파일 정의, 탭 자동완성 구현, 또는 `execute()` 로직 작성이 전혀 필요하지 않습니다.** 오직 `reload()` 메서드만 오버라이드하여 리로드 시 실행할 커스텀 로직을 정의하면 됩니다.

```java
@Override
protected void enable() {
    registerCommand(new MainCommand(this), true);
}
```

### Result (명령어 실행 결과)

`execute()` 메서드의 반환값에 따라 프레임워크가 **자동으로 공통 안내 메시지를 발송**합니다.

| Result | 설명 | 프레임워크 동작 |
|--------|------|----------------|
| `SUCCESS` | 성공 | 없음 |
| `FAILURE` | 실패 (개별 처리 필요) | 없음 — 필요 시 직접 `notifier`로 안내 |
| `ONLY_PLAYER` | 플레이어만 실행 가능 | 자동 안내 메시지 출력 |
| `ONLY_CONSOLE` | 콘솔만 실행 가능 | 자동 안내 메시지 출력 |
| `NO_PERMISSION` | 권한 없음 | 자동 안내 메시지 출력 |
| `NOT_FOUND_ONLINE_PLAYER` | 온라인 플레이어를 찾을 수 없음 | 자동 안내 메시지 출력 |
| `NOT_FOUND_OFFLINE_PLAYER` | 오프라인 플레이어를 찾을 수 없음 | 자동 안내 메시지 출력 |
| `NOT_FOUND_ITEM` | 아이템을 찾을 수 없음 | 자동 안내 메시지 출력 |
| `WRONG_USAGE` | 잘못된 사용법 | 서브 명령어 목록 및 usage 자동 표시 |

```java
// ✅ 올바른 예시
@Override
protected Result execute(CommandArgs data) {
    Player player = player();
    if (player == null) return Result.ONLY_PLAYER;
    
    notifier.announce("환영합니다!");
    return Result.SUCCESS;
}
```

```java
// ❌ 잘못된 예시 — 프레임워크가 이미 처리하는 메시지를 직접 작성
@Override
protected Result execute(CommandArgs data) {
    Player target = Bukkit.getPlayer(data.get(0));
    if (target == null) {
        getSender().sendMessage("온라인 플레이어를 찾을 수 없습니다."); // 불필요
        return Result.FAILURE;
    }
    return Result.SUCCESS;
}
```

> **메시지 전송 시 `getSender().sendMessage()`가 아닌 `notifier`를 사용합니다.** `notifier`는 MiniMessage 포맷과 접두사를 자동으로 처리합니다.  
> `RSCommand`의 `execute()`와 `tabComplete()` 내부에서는 명령어 실행자(sender/player)가 자동으로 수신자로 설정되므로, `notifier.announce("메시지")` 처럼 대상 지정 없이 사용할 수 있습니다.

### 명령어 다국어 번역 및 구조 정의

`RSCommand` 생성자에 전달되는 식별자(예: `"test"`)는 `Translation/Command/{언어}.yml` 파일에서 명령어의 이름, 설명, 사용법, 서브 명령어 등을 정의하는 최상위 키로 사용됩니다.

```yaml
# 기본 구조
test:
  name: "테스트"

# 서브 명령어 포함
test:
  name: "테스트"
  description: "테스트 명령어 입니다"
  usage: "/테스트"
  commands:
    sub:
      name: "서브"
      description: "서브 테스트 명령어 입니다"
      usage: "/테스트 서브"
```

---

## 설정 파일 관리 (Configuration)

Sponge Configurate 기반 YAML 객체 매핑을 지원합니다. `ConfigurationPart`를 상속하여 사용합니다.

> **`@SuppressWarnings` 필수** — Configurate는 리플렉션으로 필드를 직접 조작하므로, IDE 경고를 억제해야 합니다.

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
public class MyConfig extends ConfigurationPart {

    @Comment("Welcome message (MiniMessage format)")
    private String welcomeMessage = "<green>환영합니다!";

    @Min(1)
    @Comment("Maximum players")
    private int maxPlayers = 100;

    public Connection connection;

    @Getter
    public class Connection extends ConfigurationPart {
        @Comment("Server address")
        private String host = "127.0.0.1";

        @Comment("Server port")
        private int port = 25565;
    }
}
```

### 등록 및 조회

```java
import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.configurate.model.ConfigList;

@Override
protected void initialize() {
    // 단일 파일: Config/Setting.yml
    registerConfiguration(MyConfig.class, ConfigPath.of("Setting"));

    // 폴더: Config/Regions/*.yml
    registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
}

public void doSomething() {
    MyConfig config = getConfiguration(MyConfig.class);
    ConfigList<RegionConfig> regions = getConfigurations(RegionConfig.class);

    RegionConfig spawn = regions.get("spawn");     // spawn.yml
    for (RegionConfig r : regions.values()) { /* ... */ }

    // 리로드
    reloadConfiguration(MyConfig.class);
}
```

상세 내용은 `docs/configuration.md`를 참조하세요.

---

## 다국어 지원 (Translation)

플레이어 클라이언트 언어(`Locale`)에 맞춰 자동으로 번역본을 반환합니다.

```java
// Translation/Message/{locale}.yml 에서 키로 검색
String msg = message.get(player, "error.no-money");
notifier.announce(player, msg);

// 프레임워크 공통 번역
String common = message.getCommon("prefix");
```

---

## 메시지 전송 (Notifier)

MiniMessage 포맷 지원. 채팅, 액션바, 타이틀, 보스바, 크로스서버 브로드캐스트를 제공합니다.

> **`getSender().sendMessage()` 또는 `player.sendMessage()`를 직접 호출하지 마세요.** 항상 `notifier`를 통해 메시지를 전송합니다.

```java
// RSCommand 외부 (RSListener, RSInventory 등)
notifier.announce(player, "<aqua>아이템을 지급받았습니다!");       // 접두사 포함
notifier.send(player, "<yellow>경고 메시지");                    // 접두사 제외
notifier.title(player, "<bold><gold>레벨 업!", "<gray>새 스킬 해제");

// RSCommand 내부 (파라미터 생략 가능)
notifier.announce("<aqua>명령어 실행 완료!");

// 전체 서버
Notifier.broadcastAll("<green>새로운 이벤트가 시작되었습니다!");
```

---

## 브릿지 통신 (Bridge)

서버 간 **Pub/Sub** 브로드캐스트와 **RPC** 요청-응답을 지원합니다.

### Pub/Sub

```java
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.Proxium;

Proxium proxium = getBridge(Proxium.class);
BridgeChannel channel = BridgeChannel.of("myplugin", "shop");

// 타입 안전 구독
proxium.subscribe(channel, BuyRequest.class, buy -> {
    getLogger().info(buy.playerName() + "님이 구매를 요청했습니다.");
});

// 발행
proxium.publish(channel, new BuyRequest("ipecter", "DIAMOND", 64));
```

### RPC (원격 프로시저 호출)

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
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

for (ProxyPlayer p : proxium.getPlayers().values()) {
    System.out.println(p.getName() + " → " + p.getServer());
}
```

### Redis 전용 — 분산 락

```java
import kr.rtustudio.bridge.redis.Redis;

Redis redis = getBridge(Redis.class);
redis.withLock("player-data-save", () -> { /* 안전한 저장 */ });
boolean ok = redis.tryLockOnce("daily-reward", () -> { /* 보상 지급 */ });
```

상세 아키텍처는 `docs/bridge.md`를 참조하세요.

---

## 스토리지 (Storage)

JSON 문서 기반 통합 API로 모든 데이터베이스를 동일한 인터페이스(`add` / `set` / `get`)로 관리합니다.

**지원 타입**: JSON, SQLite, MySQL, MariaDB, PostgreSQL, MongoDB

```java
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;
import kr.rtustudio.storage.JSON;

// 등록
registerStorage("PlayerData", StorageType.MARIADB);

// 사용
Storage storage = getStorage("PlayerData");

// 삽입
storage.add(JSON.of("uuid", uuid.toString())
    .append("name", "IPECTER")
    .append("coins", 1000));

// 조회
storage.get(JSON.of("uuid", uuid.toString())).thenAccept(results -> {
    if (!results.isEmpty()) {
        int coins = results.get(0).get("coins").getAsInt();
    }
});

// 수정
storage.set(
    JSON.of("uuid", uuid.toString()),
    JSON.of("uuid", uuid.toString()).append("coins", 2000)
);
```

상세 내용은 `docs/storage.md`를 참조하세요.

---

## 스케줄러

### CraftScheduler (Bukkit/Paper/Folia)

Folia와 호환되며 체이닝을 통해 후속 작업을 연결할 수 있습니다.

```java
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;

CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
}).delay(task -> {
    player.setHealth(1);
}, 20L);

CraftScheduler.delay(plugin, task -> {
    plugin.getLogger().info("비동기 1초 뒤 실행");
}, 20L, true);
```

### QuartzScheduler (Cron)

```java
import kr.rtustudio.framework.bukkit.api.scheduler.QuartzScheduler;

QuartzScheduler.run("DailyReset", "0 0 0 * * ?", MyJob.class);
```

---

## 인벤토리 UI (RSInventory)

```java
import kr.rtustudio.framework.bukkit.api.inventory.RSInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

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
        return true; // 이벤트 취소
    }
}
```

---

## 커스텀 블록/아이템 통합 (Registry)

Nexo, Oraxen, ItemsAdder, MMOItems, EcoItems 등을 단일 API로 통합합니다. 식별자는 `플러그인:아이디` 형식을 사용합니다.

```java
import kr.rtustudio.framework.bukkit.api.registry.CustomItems;
import kr.rtustudio.framework.bukkit.api.registry.CustomBlocks;

ItemStack sword = CustomItems.from("mmoitems:SWORD:FIRE_SWORD");
String id = CustomItems.to(player.getInventory().getItemInMainHand());

CustomBlocks.place(location, "oraxen:custom_ore");
String blockId = CustomBlocks.to(location.getBlock());
```

---

## 빌드

```bash
./gradlew shadowJar          # 플러그인 JAR 빌드 → builds/plugin/
./gradlew spotlessApply       # 코드 포맷팅
```

**요구사항**: JDK 21+, Gradle 9.3+
