# RSFramework

모듈화된 Bukkit/Paper 플러그인 개발 프레임워크.

> **버전**: 4.2.0 · **Java**: 21 · **지원 서버**: 1.20.1+ (Spigot/Paper/Folia) · **라이선스**: GPL-3.0

---

## 프로젝트 구조

```
RSFramework/
├── LightDI/                    경량 DI 컨테이너 (kr.rtustudio.cdi)
├── Configurate/                YAML 객체 매핑 래퍼 (kr.rtustudio.configurate.model)
├── Storage/                    통합 스토리지 시스템
│   ├── Common/                 공통 API (Storage, StorageType)
│   ├── MySQL / MariaDB / PostgreSQL / MongoDB / SQLite / Json
├── Bridge/                     서버 간 Pub/Sub 브로커
│   ├── Common/                 Bridge 인터페이스, BridgeChannel, BridgeOptions
│   ├── Redisson/               Redis 구현체
│   └── Proxium/                Netty 기반 자체 프록시 통신
│       ├── Common/API          Proxium 공개 API
│       ├── Common/Core         AbstractProxium, ProxiumServer, ProxiumProxy
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

**빌드 산출물**: 루트 `shadowJar` 태스크가 모든 모듈을 하나의 플러그인 JAR로 합친다 → `builds/plugin/RSFramework-{version}.jar`

---

## 시작하기

`RSPlugin`을 상속받아 메인 클래스를 작성한다.

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

`RSPlugin`은 `onLoad` → `initialize()` → `load()` → `onEnable` → `enable()` → `onDisable` → `disable()` 순서로 라이프사이클을 제공한다.

---

## 이벤트 리스너 (RSListener)

`RSListener<T>`를 상속하면 DI를 통해 자동 등록된다.

**제공 필드** (`protected final`): `plugin`, `framework`, `message`, `command`, `notifier`

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

계층형 구조, 권한 자동 등록, 쿨다운, 탭 자동완성을 지원한다.

`RSCommand`도 `RSListener`와 동일한 `protected final` 필드를 제공한다.

```java
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class MainCommand extends RSCommand<MyPlugin> {

    public MainCommand(MyPlugin plugin) {
        super(plugin, "myplugin", PermissionDefault.OP, 5000);
        registerCommand(new SubCommand(plugin));
    }

    @Override
    protected Result execute(CommandArgs data) {
        notifier.announce(data.player(), "메인 명령어 실행됨!");
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        if (data.args().length == 1) {
            return List.of("sub", "help");
        }
        return super.tabComplete(data);
    }

    @Override
    protected void reload() {
        plugin.getLogger().info("커스텀 설정이 리로드되었습니다!");
    }
}
```

`enable()`에서 등록 시 `true`를 전달하면 `/{명령어} reload` 서브 명령어가 자동 추가된다.

```java
@Override
protected void enable() {
    framework.registerCommand(new MainCommand(this), true);
}
```

---

## 설정 파일 관리 (Configuration)

Sponge Configurate 기반 YAML 객체 매핑. `ConfigurationPart`를 상속하거나 `@ConfigSerializable` record를 사용한다.

> `@ConfigSerializable`을 일반 클래스에 붙이면 기본 생성자(NoArgsConstructor)가 필요하다.
> `record`를 사용하면 생성자 제약 없이 불변 객체를 매핑할 수 있다.

### 설정 모델 정의

```java
import kr.rtustudio.configurate.model.ConfigurationPart;

public class MyConfig extends ConfigurationPart {
    public String welcomeMessage = "<green>환영합니다!";
    public int maxPlayers = 100;
}
```

```java
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record MyConfig(String welcomeMessage, int maxPlayers) {
    public MyConfig() {
        this("<green>환영합니다!", 100);
    }
}
```

### 등록 및 조회

```java
import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.configurate.model.ConfigList;

@Override
protected void enable() {
    // 단일 파일: Config/Setting.yml
    registerConfiguration(MyConfig.class, ConfigPath.of("Setting"));
    MyConfig config = getConfiguration(MyConfig.class);

    // 폴더: Config/Regions/*.yml
    registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
    ConfigList<RegionConfig> regions = getConfigurations(RegionConfig.class);

    RegionConfig spawn = regions.get("spawn");     // spawn.yml
    for (RegionConfig r : regions.values()) { ... }
}
```

`/reload` 호출 시 파일 추가·삭제까지 자동 반영된다. 상세 내부 구조는 [docs/configuration.md](docs/configuration.md) 참조.

---

## 다국어 지원 (Translation)

플레이어 클라이언트 언어(`Locale`)에 맞춰 자동으로 번역본을 반환한다.

```java
// Translation/Message/{locale}.yml 에서 키로 검색
String msg = plugin.getConfiguration().getMessage().get(player, "error.no-money");
notifier.announce(player, msg);

// 프레임워크 공통 번역
String common = plugin.getConfiguration().getMessage().getCommon("prefix");
```

---

## 메시지 전송 (Notifier)

MiniMessage 포맷 지원. 채팅, 액션바, 타이틀, 보스바, 크로스서버 브로드캐스트를 제공한다.

```java
import kr.rtustudio.framework.bukkit.api.player.Notifier;

Notifier.of(plugin, player).announce("<aqua>아이템을 지급받았습니다!");       // 접두사 포함
Notifier.of(plugin, player).send("<yellow>경고 메시지");                    // 접두사 제외
Notifier.of(plugin, player).title("<bold><gold>레벨 업!", "<gray>새 스킬 해제");
Notifier.broadcastAll("<green>새로운 이벤트가 시작되었습니다!");              // 전체 서버
```

---

## 브릿지 통신 (Bridge)

Redis(Redisson) 또는 Proxium을 통한 서버 간 Pub/Sub 메시징. 구현체와 관계없이 동일한 코드 패턴을 사용한다.

```java
import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;

Bridge bridge = framework.getBridge(Proxium.class); // 또는 Redis.class
BridgeChannel channel = BridgeChannel.of("myplugin", "shop");

bridge.register(channel, BuyRequest.class, SellRequest.class);

bridge.subscribe(channel, packet -> {
    if (packet instanceof BuyRequest buy) {
        getLogger().info(buy.playerName() + "님이 구매를 요청했습니다.");
    }
});

bridge.publish(channel, new BuyRequest("ipecter", "DIAMOND", 64));
```

### Redis 전용 — 분산 락

```java
import kr.rtustudio.bridge.redis.Redis;

Redis redis = framework.getBridgeRegistry().get(Redis.class);
redis.withLock("player-data-save", () -> { /* 안전한 저장 */ });
boolean ok = redis.tryLockOnce("daily-reward", () -> { /* 보상 지급 */ });
```

### Proxium 전용 — 네트워크 정보

```java
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;

Proxium proxium = framework.getBridge(Proxium.class);
for (ProxyPlayer p : proxium.getPlayers().values()) {
    System.out.println(p.name() + " → " + p.server());
}
```

상세 아키텍처는 [docs/bridge.md](docs/bridge.md) 참조.

---

## 스토리지 (Storage)

다양한 데이터베이스를 통합 관리한다. 설정 변경 시 변경된 커넥션만 재연결한다.

```java
import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageType;

registerStorage("UserData", StorageType.MYSQL);

Storage storage = getStorage("UserData");
if (storage != null && storage.isConnected()) {
    Object connection = storage.getConnection();
}
```

**지원 타입**: JSON, SQLite, MySQL, MariaDB, PostgreSQL, MongoDB

상세 내용은 [docs/storage.md](docs/storage.md) 참조.

---

## 스케줄러

### CraftScheduler (Bukkit/Paper/Folia)

Folia 호환. 체이닝을 통해 후속 작업을 연결할 수 있다.

```java
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;

CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
}).delay(task -> {
    player.setHealth(1);
}, 20L);

CraftScheduler.delay(plugin, task -> {
    getLogger().info("비동기 1초 뒤 실행");
}, 20L, true);
```

### QuartzScheduler (Cron)

```java
import kr.rtustudio.framework.bukkit.api.scheduler.QuartzScheduler;

QuartzScheduler.run("DailyReset", "0 0 0 * * ?", MyJob.class);
```

---

## 인벤토리 UI (RSInventory)

`RSInventory`도 `plugin`, `framework`, `message`, `command`, `notifier`를 `protected final` 필드로 제공한다.

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

Nexo, Oraxen, ItemsAdder, MMOItems, EcoItems 등을 단일 API로 통합한다. 식별자는 `플러그인:아이디` 형식.

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
