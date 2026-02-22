# RSFramework

강력하고 모듈화된 Bukkit/Paper 플러그인 개발 프레임워크입니다.
이 문서는 **API 모듈을 활용하여 플러그인을 개발하는 방법**을 중심으로 설명합니다.

내부 기술 스택, 시스템 아키텍처 및 상세 원리는 `docs/` 폴더의 문서를 참조하세요.

---

## 📌 목차

1. [시작하기](#-시작하기)
2. [플러그인 설정 (RSPlugin)](#-플러그인-설정-rsplugin)
3. [이벤트 리스너 (RSListener)](#-이벤트-리스너-rslistener)
4. [명령어 시스템 (RSCommand)](#-명령어-시스템-rscommand)
5. [설정 파일 관리 (Configuration)](#-설정-파일-관리-configuration)
6. [다국어 지원 (Translation)](#-다국어-지원-translation)
7. [메시지 전송 (Notifier)](#-메시지-전송-notifier)
8. [브릿지 통신 (Bridge)](#-브릿지-통신-bridge)
9. [스케줄러 (CraftScheduler & QuartzScheduler)](#-스케줄러-craftscheduler--quartzscheduler)
10. [인벤토리 UI (RSInventory)](#-인벤토리-ui-rsinventory)
11. [커스텀 블록/아이템/가구 통합](#-커스텀-블록아이템가구-통합)

---

## 🚀 시작하기

`RSPlugin`을 상속받아 메인 클래스를 작성합니다.

```java
import kr.rtustudio.framework.bukkit.api.RSPlugin;

public class MyPlugin extends RSPlugin {
    
    @Override
    protected void enable() {
        getLogger().info("플러그인이 활성화되었습니다!");
        
        // 커스텀 로직 초기화
    }
    
    @Override
    protected void disable() {
        getLogger().info("플러그인이 비활성화되었습니다!");
    }
}
```

---

## 🎧 이벤트 리스너 (RSListener)

`RSListener<T>`를 상속받아 이벤트를 등록합니다. 별도의 수동 등록 없이 DI를 통해 자동 등록됩니다.

`RSListener`는 아래의 `protected final` 필드를 제공하므로, 상속한 클래스에서 getter 호출 없이 바로 사용할 수 있습니다.

| 필드          | 타입                   | 설명           |
|-------------|----------------------|--------------|
| `plugin`    | `T`                  | 소유 플러그인 인스턴스 |
| `framework` | `Framework`          | 프레임워크 인스턴스   |
| `message`   | `MessageTranslation` | 메시지 번역       |
| `command`   | `CommandTranslation` | 명령어 번역       |
| `notifier`  | `Notifier`           | 메시지 전송 유틸리티  |

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
        // Notifier를 사용해 플레이어에게 환영 메시지 전송
        notifier.announce(event.getPlayer(), "<green>서버에 오신 것을 환영합니다!");
    }
}
```

---

## ⌨️ 명령어 시스템 (RSCommand)

계층형 구조, 권한 검사, 쿨다운, 탭 자동완성을 지원하는 명령어 시스템입니다.

`RSCommand`도 `RSListener`와 동일하게 `plugin`, `framework`, `message`, `command`, `notifier`를 `protected final` 필드로 제공하므로,
상속한 클래스에서 바로 사용할 수 있습니다.

```java
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class MainCommand extends RSCommand<MyPlugin> {

    public MainCommand(MyPlugin plugin) {
        // 명령어 이름 "myplugin", 쿨다운 5초 지정
        super(plugin, "myplugin", PermissionDefault.OP, 5000);

        // 서브 명령어 등록 (명령어 이름 뒤에 .으로 노드가 추가되며 권한이 자동 등록됨)
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
        // /myplugin reload 명령어 실행 시 자동 호출됨
        plugin.getLogger().info("커스텀 설정이 리로드되었습니다!");
    }
}
```

생성한 명령어는 `RSPlugin`의 `enable()` 메서드 내에서 `registerCommand`를 호출하여 등록합니다. `true`를 전달하면 `/{명령어} reload` 서브 명령어가 자동으로 추가되며,
실행 시 프레임워크의 설정 파일/번역 파일 리로드 및 명령어 클래스의 `reload()` 메서드가 호출됩니다.

```java

@Override
protected void enable() {
    // 명령어 등록 및 자동 reload 서브 명령어 추가
    framework.registerCommand(new MainCommand(this), true);
}
```

---

## ⚙️ 설정 파일 관리 (Configuration)

Configurate 기반으로 YAML 파일을 자바 객체로 매핑합니다.
`ConfigurationPart`를 상속받아 데이터 모델을 정의하고, `RSPlugin`에서 등록합니다.

> **💡 `@ConfigSerializable` 사용 시 주의사항**
> `Configurate` 라이브러리의 특성상, 일반 클래스에 `@ConfigSerializable`을 붙일 경우 파라미터가 없는 기본 생성자(NoArgsConstructor)가 반드시 필요합니다.
> 만약 자바의 `record` 클래스(레코드)를 사용한다면 생성자 제약 없이 훨씬 깔끔하게 데이터 불변 객체를 직렬화/역직렬화할 수 있습니다.

### 1. 설정 모델 정의

`ConfigurationPart`를 상속받거나 `@ConfigSerializable`을 사용하여 데이터 모델을 정의합니다.

#### 예시 1: 일반 클래스 (기본 생성자 필요)

```java
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;

public class MyConfig extends ConfigurationPart {
    public String welcomeMessage = "<green>환영합니다!";
    public int maxPlayers = 100;
}
```

#### 예시 2: `record` 클래스 (`@ConfigSerializable` 사용)

직렬화/역직렬화하려는 데이터 전용 `record` 클래스에 `@ConfigSerializable`을 붙여 사용할 수 있습니다.

```java
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record MyConfig(String welcomeMessage, int maxPlayers) {
    // 기본값이 필요할 경우
    public MyConfig() {
        this("<green>환영합니다!", 100);
    }
}
```

### 2. 설정 등록 및 가져오기

플러그인 시작 시 단일 설정 파일이나, 디렉토리 내의 설정 파일 목록(`ConfigList`)을 등록하고, 이후 어디서든 쉽게 가져와 사용할 수 있습니다.
모든 설정은 내부적으로 캐싱되어 관리되며, `/reload` 명령어나 `reloadAll()` 호출 시 폴더 내 파일 변경사항(추가/삭제)까지 자동으로 반영됩니다.

#### 단일 파일 등록 및 가져오기

```java
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;

public class MyPlugin extends RSPlugin {

    @Override
    public void enable() {
        // Config/Setting.yml 단일 파일 등록 및 로드
        registerConfiguration(MyConfig.class, ConfigPath.of("Setting"));

        // 언제 어디서든 등록된 단일 설정을 클래스 타입으로 가져옴
        MyConfig config = getConfiguration(MyConfig.class);
        getLogger().info("메시지: " + config.welcomeMessage);
    }
}
```

#### 다중 파일 목록(ConfigList) 등록 및 가져오기

```java
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigList;

public class MyPlugin extends RSPlugin {

    @Override
    public void enable() {
        // Config/Regions 폴더 안의 모든 yml 파일을 등록 및 로드
        registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));

        // 언제 어디서든 등록된 설정 목록을 가져옴
        ConfigList<RegionConfig> regions = getConfigurations(RegionConfig.class);

        // 파일명(확장자 제외)으로 특정 설정 접근
        RegionConfig spawn = regions.get("spawn");

        // 모든 설정 순회
        for (RegionConfig region : regions.values()) {
            // ...
        }
    }
}
```

---

## 🌐 다국어 지원 (Translation)

`RSPlugin.getConfiguration().getMessage()` 또는 `getCommand()`를 통해 다국어 번역을 쉽게 가져옵니다.
플레이어의 클라이언트 언어(`Locale`)에 맞춰 자동으로 번역본이 반환됩니다.

```java
// Translation/Message/ko.yml 또는 en_us.yml 등에서 "error.no-money" 키를 찾아 반환
String msg = plugin.getConfiguration().getMessage().get(player, "error.no-money");
notifier.

announce(player, msg);

// 공통 번역 (Framework 모듈 제공)
String common = plugin.getConfiguration().getMessage().getCommon("prefix");
```

---

## 💬 메시지 전송 (Notifier)

`Notifier`는 `MiniMessage` 포맷(예: `<red>텍스트`)을 지원하며 액션바, 타이틀, 보스바 등 다양한 출력을 지원합니다.

```java
import kr.rtustudio.framework.bukkit.api.player.Notifier;

// 1. 단일 대상 전송 (접두사 포함)
Notifier.of(plugin, player).

announce("<aqua>아이템을 지급받았습니다!");

// 2. 단일 대상 전송 (접두사 제외)
Notifier.

of(plugin, player).

send("<yellow>경고 메시지");

// 3. 타이틀 및 서브타이틀
Notifier.

of(plugin, player).

title("<bold><gold>레벨 업!","<gray>새로운 스킬이 해제되었습니다.");

// 4. 서버 전체 브로드캐스트 (ProtoWeaver 연결 시 크로스 서버 전송)
Notifier.

broadcastAll("<green>새로운 이벤트가 시작되었습니다!");
```

---

## 📡 브릿지 통신 (Bridge)

Redis나 자체 프록시 채널(ProtoWeaver)을 통해 서버 간 분산 메시징을 완벽하게 지원합니다.
`BridgeChannel`을 통해 네임스페이스와 키를 명확하게 분리하여 관리합니다.

```java
import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.protoweaver.bukkit.api.ProtoWeaver;

// 브릿지 구현체 가져오기 (Redis.class 등 가능)
Bridge bridge = framework.getBridgeRegistry().get(Redis.class); // 또는 ProtoWeaver.class
        BridgeChannel channel = BridgeChannel.of("myplugin", "shop");

// 1. 채널 등록 (사용할 데이터 클래스 지정)
bridge.

        register(channel, BuyRequest .class, SellRequest .class);

// 2. 메시지 수신(구독)
bridge.

        subscribe(channel, packet ->{
        if(packet instanceof
        BuyRequest buy){

        getLogger().

        info(buy.playerName() +"님이 구매를 요청했습니다.");
        }
        });

// 3. 메시지 발송(발행)
        bridge.

        publish(channel, new BuyRequest("ipecter", "DIAMOND",64));
```

### 구현체 전용 특화 기능

일부 기능은 특정 브릿지 구현체(`Redis` 또는 `ProtoWeaver`)에서만 제공됩니다. 이를 사용하려면 해당 타입으로 캐스팅하거나 레지스트리에서 직접 해당 타입을 가져와야 합니다.

#### 1. Redis 전용 기능 (분산 락)

Redis 브릿지는 다중 서버 간의 동시성 제어를 위한 **분산 락(Distributed Lock)**을 제공합니다.

```java
import kr.rtustudio.bridge.redis.Redis;

Redis redis = framework.getBridgeRegistry().get(Redis.class);

// 동기식 분산 락 실행 (락 획득 시까지 대기)
redis.

withLock("player-data-save",() ->{
        // 안전한 데이터 저장 로직
        });

// 한 번만 실행되는 락 (동시 다발적 요청 중 하나만 실행)
boolean success = redis.tryLockOnce("daily-reward", () -> {
    // 보상 지급 로직
});
```

#### 2. ProtoWeaver 전용 기능 (프록시 데이터 접근)

ProtoWeaver 브릿지는 프록시 서버(BungeeCord/Velocity)에 연결된 전체 네트워크 플레이어 및 서버 정보에 접근할 수 있습니다.

```java
import kr.rtustudio.bridge.protoweaver.bukkit.api.ProtoWeaver;
import kr.rtustudio.bridge.protoweaver.api.proxy.ProxyPlayer;

ProtoWeaver proxy = framework.getBridgeRegistry().get(ProtoWeaver.class);

// 네트워크 전체 접속자 목록 조회
for(
ProxyPlayer p :proxy.

getPlayers()){
        System.out.

println(p.name() +"님은 현재 "+p.

server() +" 서버에 있습니다.");
        }

// 특정 플레이어가 프록시 네트워크에 접속해 있는지 확인
        proxy.

getPlayer("ipecter").

ifPresent(p ->{
        System.out.

println("핑: "+p.ping() +"ms");
        });
```

---

## ⏱️ 스케줄러 (CraftScheduler & QuartzScheduler)

### CraftScheduler (Bukkit/Paper/Folia 대응)

Folia 환경과 100% 호환되는 스케줄러입니다. 생성된 스케줄 객체(`ScheduledTask`)를 반환하며 **체이닝(Chaining)**을 통해 후속 작업을 연결할 수 있습니다.

```java
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;

// 동기 실행 후 20틱(1초) 뒤 다른 작업 체이닝 연결
CraftScheduler.sync(plugin, task ->{
        player.

setHealth(20);
    player.

sendMessage("체력이 회복되었습니다.");
}).

delay(task ->{
        player.

setHealth(1);
    player.

sendMessage("1초 뒤 다시 체력이 1이 되었습니다.");
},20L);

// 비동기 지연 실행 (20틱 = 1초)
        CraftScheduler.

delay(plugin, task ->{

getLogger().

info("비동기로 1초 뒤 실행");
},20L,true);
```

### QuartzScheduler (실시간/Cron 기반)

특정 시각이나 복잡한 주기(`Cron`)로 실행해야 할 때 사용합니다.

```java
import kr.rtustudio.framework.bukkit.api.scheduler.QuartzScheduler;
import org.quartz.Job;

// 매일 자정에 실행
QuartzScheduler.run("DailyReset","0 0 0 * * ?",MyJob .class);
```

---

## 🎒 인벤토리 UI (RSInventory)

커스텀 인벤토리 GUI를 쉽게 제작할 수 있는 기반 클래스입니다.

`RSInventory`도 마찬가지로 `plugin`, `framework`, `message`, `command`, `notifier`를 `protected final` 필드로 제공합니다.

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
        // true 반환 시 이벤트 취소 (아이템 이동 방지)
        notifier.announce(event.player(), "슬롯 " + click.slot() + " 클릭됨!");
        return true;
    }
}
```

---

## 📦 커스텀 블록/아이템/가구 통합 (Registry)

Nexo, Oraxen, ItemsAdder, MMOItems, EcoItems 등의 타사 플러그인을 단일 API로 통합 관리합니다.
모든 식별자는 `플러그인:아이디` 형태의 **Namespaced ID**를 사용합니다.

### CustomItems

```java
import kr.rtustudio.framework.bukkit.api.registry.CustomItems;

// 아이템 가져오기
ItemStack sword = CustomItems.from("mmoitems:SWORD:FIRE_SWORD");
        ItemStack nexoBlock = CustomItems.from("nexo:ruby_block");

        // 아이템을 식별자로 변환
        String id = CustomItems.to(player.getInventory().getItemInMainHand());

        // NBT / Base64 직렬화
        String serialized = CustomItems.serialize(sword, true); // 압축
```

### CustomBlocks

```java
import kr.rtustudio.framework.bukkit.api.registry.CustomBlocks;

// 지정 위치에 커스텀 블록 설치
CustomBlocks.place(location, "oraxen:custom_ore");

// 블록 정보 가져오기
String blockId = CustomBlocks.to(location.getBlock());
```
