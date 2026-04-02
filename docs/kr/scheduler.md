# Scheduler

RSFramework는 Bukkit, Paper, Folia를 아우르는 통합 스케줄러 시스템을 지원합니다. 기존의 `BukkitRunnable`이나 `Bukkit.getScheduler()`를 직접 관리할 필요 없이, Fluent API 체이닝이나 `CompletableFuture` 기반의 모던한 방식으로 직관적인 비동기 처리를 구현할 수 있습니다.

---

## 1. 지연/반복 체이닝 타스크

`CraftScheduler`를 통해 체인(Chain) 형태로 태스크를 정의하여 스레드 간 전환을 쉽고 가독성 있게 작성할 수 있습니다. 플러그인이 종료되면 해당 플러그인의 모든 작업이 알아서 취소(Cancel)되기 때문에 메모리 릭 걱정이 줄어듭니다.

```java
// 동기(메인 스레드)에서 실행 후 비동기 지연 실행
CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
    // ... 메인 스레드 코드 ...
}).delay(task -> {
    // 20틱(1초) 후 메인 스레드에서 실행
    player.setHealth(1);
}, 20L);
```

### Folia Location/Entity 특정 스케줄링
Folia 환경에서는 대륙(Region)이나 개체(Entity)별로 스레드가 분할되므로 글로벌 틱이 아닌 **특정 타겟**을 전달해야 합니다. RSFramework는 `Location`이나 `Entity`를 첫 번째 파라미터로 넘겨주면 내부적으로 Folia의 `RegionScheduler`나 `EntityScheduler`로 자동 매핑합니다. (Spigot/Paper 환경에서는 그대로 메인 스레드에서 돌아가므로 완전히 호환됩니다.)

```java
// 거미가 위치한 스레드 영역 안에서 타스크 실행
CraftScheduler.sync(spider, task -> {
    spider.setTarget(player);
});

// 특정 월드 좌표 공간 스케줄링
CraftScheduler.delay(location, task -> {
    location.getBlock().setType(Material.DIAMOND_BLOCK);
}, 60L /*3초 뒤*/);
```

---

## 2. 안전한 동기 결과 반환: `callSync` (CompletableFuture)

Spigot/Paper의 순정 스케줄러 API 중 `callSyncMethod()`는 데이터 읽기 시 자주 쓰였지만, Folia에서는 사용할 수 없습니다(`UnsupportedOperationException` 발생). 게다가 메인 스레드를 무한히 대기시킬 위험성 때문에 기피되어 왔습니다.

이 단점을 해결하기 위해 `CraftScheduler.callSync`가 도입되었습니다. 
결과를 스레드 대기(Blocking) 없이 안전하게 받아올 수 있도록 **`CompletableFuture<T>`** 객체를 즉각적으로 반환합니다.

### 특징 및 모범 규칙 (Best Practice)
- **절대 `join()`을 강제로 수행하지 않습니다.** 반환된 `CompletableFuture` 객체를 통해 `thenAccept` 체이닝으로 콜백 처리를 해주세요.
- **가장 모던합니다.** Folia의 지역 틱(Region Tick) 분할 처리를 프레임워크가 알아서 연동해주어 별도의 컴파일/분기 작업이 필요하지 않습니다.

**✅ [권장] `thenAccept`를 이용해 비동기로 결과 소비하기:**
```java
// 메인 스레드, 혹은 Folia 글로벌 스레드에서 즉시 정보 취합
CraftScheduler.callSync(() -> {
    return Bukkit.getOnlinePlayers().size();
}).thenAccept(playerCount -> {
    System.out.println("현재 모든 서버 접속자 수: " + playerCount);
});

// 특정 상자(Location 위치)의 데이터 안전하게 읽기
Location chestLoc = new Location(world, 100, 64, 100);

CraftScheduler.callSync(chestLoc, () -> {
    Block block = chestLoc.getBlock();
    if (block.getState() instanceof Chest chest) {
        return chest.getInventory().getContents();
    }
    return new ItemStack[0];
}).thenAccept(items -> {
    // 아이템 정보 로드 후, 랙과 무관하게 필요한 화면 출력 진행!
    player.sendMessage("아이템 종류: " + items.length + "개");
}).exceptionally(ex -> {
    player.sendMessage("상자를 찾거나 스레드에 접근하는 데 실패했습니다.");
    return null;
});
```

**⚠️ [주의] `join()` 또는 `get()` 사용 시의 위험 사항**
`callSync(...).join()`은 꼭 필요한 **순수 비동기(Async) 백그라운드 스레드** 내부에서만 작성하세요. (예: DB 저장 스레드, 외부 통신 수신 스레드 등)
만약 인게임 메인/리전 스레드(Tick Thread)가 동작 중일 때 이 메서드에 `join()`을 걸고 기다리면, **서버에 엄청난 랙이 걸리거나 Folia 서버의 워치독(WatchDog)이 스레드 데드락으로 간주해 서버를 강제로 터뜨릴 수 있습니다!**

---

## 3. QuartzScheduler (예약/반복 작업)

매 시 정각이나 자정마다 호출해야 하는 보상 시스템이나 초기화 시스템을 제작할 땐 Cron 표현식을 사용하는 Quartz 스케줄러를 활용할 수 있습니다.

```java
// 매일 자정(0시 0분 0초)에 MyJob.class 호출
QuartzScheduler.run("DailyReset", "0 0 0 * * ?", MyJob.class);
```
※ `MyJob.class`는 Quartz API의 `Job` 인터페이스를 상속해서 구현하면 됩니다.
