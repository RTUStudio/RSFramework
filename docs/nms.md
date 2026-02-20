# NMS & Platform 추상화

## 1. 개요

RSFramework는 서버 버전 파편화 및 플랫폼 파편화(Spigot, Paper, Folia)를 극복하기 위한 강력한 추상화 레이어를 제공합니다.
플러그인 개발자는 `Framework:API`에서 제공하는 인터페이스만 사용하면, 런타임에 자동으로 현재 서버 버전에 맞는 구현체가 로드되어 실행됩니다.

지원 버전: **1.20.1 ~ 1.21.x** (지속 업데이트)

---

## 2. NMS 인터페이스 (`NMS`)

```java
public interface NMS {

    Item getItem();

    Biome getBiome();

    Command getCommand();
}
```

### 2.1. 인스턴스 획득

```java
NMS nms = plugin.getFramework().getNMS();
```

### 2.2. Biome (바이옴 조작)

```java
// 특정 위치의 바이옴 키(NamespacedKey) 조회
String biomeKey = nms.getBiome().getKey(location);

// 커스텀 바이옴 등록 (버전별 레지스트리 조작)
nms.getBiome().register(key, biomeData);
```

### 2.3. Command (명령어)

```java
// Brigadier 명령어 트리 등록
nms.getCommand().register(commandNode);

// 플레이어에게 명령어 목록 동기화 요청 (패킷 전송)
nms.getCommand().sync(player);
```

### 2.4. Item (아이템 NBT 및 메타데이터)

```java
// NBT 태그 안전하게 읽기/쓰기
ItemStack item = nms.getItem().setNBT(itemStack, "custom_key", "value");
String value = nms.getItem().getNBT(itemStack, "custom_key");

// 커스텀 모델 데이터 부여 (버전 호환)
ItemStack item = nms.getItem().setCustomModelData(itemStack, 1001);
```

---

## 3. 버전별 NMS 구현체 구조

NMS 코드는 `Framework:NMS` 하위 모듈들로 완벽하게 분리되어 컴파일됩니다.
각 모듈은 `paperweight.paperDevBundle`을 사용하여 각 버전에 맞는 매핑을 사용합니다.

| 모듈 | 클래스 | 타겟 서버 버전 |
|------|--------|-----------|
| `Framework:NMS:1_20_R1` | `NMS_1_20_R1` | 1.20.1 |
| `Framework:NMS:1_20_R2` | `NMS_1_20_R2` | 1.20.2 |
| `Framework:NMS:1_20_R3` | `NMS_1_20_R3` | 1.20.3, 1.20.4 |
| `Framework:NMS:1_20_R4` | `NMS_1_20_R4` | 1.20.5, 1.20.6 |
| `Framework:NMS:1_21_R1` | `NMS_1_21_R1` | 1.21, 1.21.1 |
| `Framework:NMS:1_21_R2` | `NMS_1_21_R2` | 1.21.2, 1.21.3 |
| `Framework:NMS:1_21_R3` | `NMS_1_21_R3` | 1.21.4 |
| `Framework:NMS:1_21_R4` | `NMS_1_21_R4` | 1.21.5 |
| `Framework:NMS:1_21_R5` | `NMS_1_21_R5` | 1.21.8 |
| `Framework:NMS:1_21_R6` | `NMS_1_21_R6` | 1.21.10 |
| `Framework:NMS:1_21_R7` | `NMS_1_21_R7` | 1.21.11 |

> **동작 원리**: 서버가 켜질 때 `MinecraftVersion.current()`를 통해 버전을 파악한 뒤, 리플렉션으로 알맞은 클래스를 동적 로드합니다.

---

## 4. 버전 유틸리티 (`MinecraftVersion`)

현재 서버의 버전을 확인하거나 비교할 수 있는 유틸리티를 제공합니다.

```java
// 1. 현재 구동 중인 서버 버전 획득 (예: "1.20.1")
String version = MinecraftVersion.getAsText();
MinecraftVersion.Version v = MinecraftVersion.get();

// 2. 플랫폼 확인
boolean isPaper = MinecraftVersion.isPaper();
boolean isFolia = MinecraftVersion.isFolia();

// 3. 특정 버전 이상 지원 여부 확인 (예: 1.20.1 이상인가?)
boolean supported = MinecraftVersion.isSupport("1.20.1");

// 4. 최소~최대 버전 범위 확인
boolean inRange = MinecraftVersion.isSupport("1.20.1", "1.21.1");
```

---

## 5. Platform 및 스케줄러 추상화

RSFramework는 `Platform:Spigot`, `Platform:Paper`, `Platform:Folia` 등 플랫폼별 차이를 `CraftScheduler`를 통해 추상화합니다.

### 5.1. Folia 완벽 지원
기존의 Bukkit Task 체계는 멀티스레드 기반인 Folia에서 동작하지 않습니다.
하지만 RSFramework의 `CraftScheduler`를 사용하면 환경을 감지하여 자동으로 올바른 스케줄러로 라우팅합니다.

### 5.2. 간편한 Static 메서드 사용 (권장)
플러그인 인스턴스를 통해 스케줄러를 가져오는 것보다, `CraftScheduler`의 static 메서드를 직접 호출하는 것이 가장 권장되는 방식입니다.

```java
// 동기 작업 (Folia의 경우 GlobalRegionScheduler 사용)
CraftScheduler.sync(() -> {
    System.out.println("Synchronous task");
});

// 비동기 작업
CraftScheduler.async(() -> {
    System.out.println("Asynchronous task");
});

// 지연 후 동기 작업 (예: 20틱 후)
CraftScheduler.delay(() -> {
    System.out.println("Delayed task");
}, 20L);

// 반복 작업 (예: 20틱 지연, 10틱마다 반복)
CraftScheduler.repeat(() -> {
    System.out.println("Repeating task");
}, 20L, 10L);

// Location 기반 작업 (Folia RegionScheduler 라우팅)
CraftScheduler.sync(location, () -> {
    player.teleport(location);
});

// Entity 기반 작업 (Folia EntityScheduler 라우팅)
CraftScheduler.sync(entity, () -> {
    entity.setFireTicks(100);
});
```

### 5.3. 체이닝(Fluent API)을 활용한 스케줄링
`CraftScheduler`의 static 메서드가 반환하는 `ScheduledTask` (또는 내부 빌더)를 활용하면, 연쇄적인(체이닝) 방식으로 직관적인 스케줄링이 가능합니다. 이 역시 static 메서드로 시작하는 것을 권장합니다.

```java
CraftScheduler.sync(() -> System.out.println("Step 1"))
    .delay(20L)
    .run(() -> System.out.println("Step 2 after 1 second"))
    .repeat(10L); // 이후 0.5초마다 반복 실행
```
