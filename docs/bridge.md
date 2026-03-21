# Bridge 시스템 가이드

> RSFramework의 **Bridge**는 프록시-백엔드 서버 간 실시간 통신 계층입니다.  
> **Pub/Sub** 브로드캐스트와 **RPC** 요청-응답을 지원합니다.

---

## 1. 아키텍처

### 인터페이스 계층

```
Bridge (공통 인터페이스)
├── Redisson (Redis Pub/Sub 기반)
└── Proxium (Netty 다이렉트 채널)
```

| 인터페이스 | 대상 | 핵심 기능 |
|-----------|------|----------|
| `Bridge` | 모든 브릿지 공통 | `register` · `subscribe` · `publish` · `unsubscribe` |
| `Proxium` | 플러그인 개발자 | `request` · `respond` · `getPlayers` · `getServer` |

### 인스턴스 얻기

```java
import kr.rtustudio.bridge.proxium.api.Proxium;

// RSPlugin 내부에서
Proxium proxium = getFramework().getBridge(Proxium.class);

// 연결 상태 확인
if (proxium == null || !proxium.isLoaded()) {
    getLogger().warning("Proxium이 로드되지 않았습니다.");
    return;
}

// 프록시에 연결되어 있는지 확인
String serverName = proxium.getServer();
if (serverName == null) {
    getLogger().warning("프록시에 연결되어 있지 않습니다.");
    return;
}
```

---

## 2. 채널과 타입 등록

모든 통신은 **채널(`BridgeChannel`)** 단위로 이루어집니다.  
채널에 사용할 데이터 클래스를 **등록**해야 직렬화/역직렬화가 가능합니다.

### 채널 생성

```java
import kr.rtustudio.bridge.BridgeChannel;

// 플러그인 전용 채널 (namespace:key 형태)
BridgeChannel channel = BridgeChannel.of("myplugin", "economy");

// 내장 채널 상수
BridgeChannel.INTERNAL;   // rsframework:internal — 프레임워크 내부 통신
BridgeChannel.AUDIENCE;   // rsframework:audience — 브로드캐스트 메시지
```

### 수동 등록

```java
proxium.register(channel, BalanceRequest.class, BalanceResponse.class);
```

> [!TIP]
> **자동 등록** — `subscribe`, `request`, `respond`에서 타입을 전달하면 **자동으로 등록**됩니다.
> 대부분의 경우 `register()`를 직접 호출할 필요가 없습니다.
>
> | 메서드 | 자동 등록 대상 |
> |--------|---------------|
> | `subscribe(channel, Type.class, handler)` | 구독 메시지 타입 |
> | `request(...).on(Type.class, handler)` | 요청 객체 + 응답 타입 |
> | `respond(channel).on(Type.class, handler)` | 수신 요청 타입 |

> [!IMPORTANT]
> **Fory 직렬화 제약** — 타입 등록은 반드시 **서버 시작 직후** (onEnable 등) 수행해야 합니다.
> 서버가 이미 통신을 시작한 이후에 새로운 타입을 등록하면 Fory의 late-registration 오류가 발생할 수 있습니다.
> `subscribe()`, `request()`, `respond()` 사용 시 타입이 자동 등록되므로 가능한 일찍 호출하세요.

---

## 3. Pub/Sub (발행 / 구독)

채널을 구독하고 있는 **모든 노드**에게 메시지를 브로드캐스트합니다.  
`Bridge` 인터페이스 기능이므로 Proxium · Redis 모두 동일하게 사용 가능합니다.

### 구독

```java
// register 호출 불필요 — subscribe 내부에서 자동 등록
proxium.subscribe(channel, TradePacket.class, trade -> {
    player.sendMessage(trade.getSender() + "님이 " + trade.getItem() + " 교환 요청");
});

// 동일 채널에 여러 타입 핸들러를 개별 등록 가능
proxium.subscribe(channel, ChatPacket.class, chat -> {
    Bukkit.broadcast(Component.text("[Chat] " + chat.message()));
});
```

> [!NOTE]
> 같은 채널에 같은 타입으로 다시 `subscribe()`를 호출하면 기존 핸들러가 **교체**됩니다.  
> 다른 타입이면 기존 핸들러를 유지하면서 **추가**됩니다.

### 발행

```java
proxium.publish(channel, new TradePacket("IPECTER", "Diamond", 64));
```

### 구독 취소

```java
proxium.unsubscribe(channel);
```

### Pub/Sub 실전 예제: 서버 간 공지 시스템

```java
// 데이터 클래스
public record Announcement(String sender, String message, long timestamp) {}

// ─── 모든 서버에서 (onEnable) ───
BridgeChannel channel = BridgeChannel.of("myplugin", "announce");

proxium.subscribe(channel, Announcement.class, announce -> {
    Bukkit.getScheduler().runTask(plugin, () -> {
        Bukkit.broadcast(Component.text("[" + announce.sender() + "] " + announce.message()));
    });
});

// ─── 관리자 명령어 실행 시 ───
proxium.publish(channel, new Announcement("Admin", "서버 점검 안내: 30분 후 점검 예정", System.currentTimeMillis()));
```

---

## 4. RPC (원격 프로시저 호출)

`Proxium` 전용 기능입니다. 특정 서버에 **1:1 요청을 보내고 응답을 받습니다.**

### 4.1. 데이터 클래스 정의

RPC에 사용할 요청/응답 클래스를 먼저 정의합니다. POJO, record 등 어떤 형태든 가능합니다.

```java
// 요청
public record BalanceRequest(UUID uuid) {}

// 응답
public record BalanceResponse(UUID uuid, double balance) {}
```

### 4.2. 응답 핸들러 등록 — `respond()`

데이터를 **보유한 서버**에서 호출합니다.  
`respond(channel)`은 `ResponseContext`를 반환하며, `.on()`으로 요청 타입별 핸들러를 체이닝합니다.

```java
proxium.respond(channel)
    .on(BalanceRequest.class, (sender, request) -> {
        // sender = 요청을 보낸 서버 이름 (예: "Lobby-1")
        double balance = getBalance(request.uuid());
        return new BalanceResponse(request.uuid(), balance);
    })
    .on(TransferRequest.class, (sender, request) -> {
        boolean ok = transfer(request.from(), request.to(), request.amount());
        return new TransferResponse(ok, ok ? "성공" : "잔액 부족");
    })
    .error(e -> {
        // 핸들러 실행 중 예외 발생 시
        log.error("RPC 응답 실패: [{}] {}", e.type(), e.getMessage());
    });
```

> [!NOTE]
> `.on()`의 핸들러 시그니처는 `ResponseHandler<T, R>` 입니다:
> ```java
> R handle(String sender, T request) throws Exception;
> ```
> 
> 동일 채널에 `respond()`를 여러 번 호출해도 기존 핸들러는 유지됩니다.
> 같은 타입으로 다시 등록하면 해당 타입의 핸들러만 교체됩니다.

### 4.3. 요청 전송 — `request()`

데이터를 **필요로 하는 서버**에서 호출합니다.  
`request()`는 `RequestContext`를 반환하며, `.on()`으로 응답 타입별 핸들러를 체이닝합니다.

```java
// 타임아웃 명시
proxium.request("Survival-1", channel,
        new BalanceRequest(player.getUniqueId()),
        Duration.ofSeconds(5))
    .on(BalanceResponse.class, (sender, response) -> {
        // sender = 응답을 보낸 서버 이름 (예: "Survival-1")
        player.sendMessage("잔고: " + response.balance() + "원");
    })
    .error(e -> {
        player.sendMessage("요청 실패: " + e.type());
    });

// 타임아웃 생략 → 구성 파일의 request-timeout 값 사용 (기본 5초)
proxium.request("Survival-1", channel, new BalanceRequest(uuid))
    .on(BalanceResponse.class, (sender, response) -> { /* ... */ })
    .error(e -> { /* ... */ });
```

### 4.4. 여러 응답 타입 처리

하나의 채널에서 서버가 요청에 따라 다른 타입의 응답을 반환할 수 있습니다.  
`.on()`을 여러 번 체이닝하면 타입에 맞는 핸들러만 자동 호출됩니다.

```java
proxium.request("Auth-Server", channel, new LoginRequest(name, password))
    .on(LoginSuccess.class, (sender, success) -> {
        player.sendMessage("로그인 성공! 등급: " + success.rank());
    })
    .on(LoginFailure.class, (sender, failure) -> {
        player.sendMessage("로그인 실패: " + failure.reason());
    })
    .error(e -> player.sendMessage("서버 통신 오류"));
```

### 4.5. CompletableFuture 변환

fluent 체이닝 대신 `CompletableFuture`로 직접 다루고 싶을 때:

```java
CompletableFuture<BalanceResponse> future = proxium
    .request("Survival-1", channel, new BalanceRequest(uuid), Duration.ofSeconds(5))
    .asFuture(BalanceResponse.class);

// 비동기 후속 처리
future.thenAccept(res -> player.sendMessage("잔고: " + res.balance()));

// 블로킹 (메인 스레드에서 호출 금지!)
BalanceResponse res = future.join();
```

---

## 5. 에러 처리

### RequestException

RPC 실패 시 `.error()` 핸들러에 `RequestException`이 전달됩니다.

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `type()` | `ResponseStatus` | 실패 원인 분류 |
| `cause()` | `Throwable?` | 원인 예외 (= `getCause()`) |
| `getMessage()` | `String` | 에러 메시지 |

### ResponseStatus

| 상태 | 의미 | 발생 위치 |
|------|------|----------|
| `SUCCESS` | 정상 처리 | — |
| `NO_HANDLER` | 대상 서버에 해당 타입의 핸들러 없음 | 원격 |
| `ERROR` | 핸들러 실행 중 예외 발생 | 원격 |
| `TIMEOUT` | 응답 대기 시간 초과 | 로컬 |

```java
.error(e -> {
    switch (e.type()) {
        case TIMEOUT    -> player.sendMessage("서버 응답 시간 초과");
        case NO_HANDLER -> player.sendMessage("서비스를 찾을 수 없습니다");
        case ERROR      -> {
            player.sendMessage("서버 내부 오류");
            log.error("원인: ", e.cause());
        }
    }
});
```

---

## 6. 플레이어 API

프록시에 접속 중인 플레이어 정보를 가져올 수 있습니다.

```java
// 전체 플레이어 조회
Map<UUID, ProxyPlayer> players = proxium.getPlayers();

// 특정 플레이어 조회
ProxyPlayer player = proxium.getPlayer(uuid);
if (player != null) {
    String name = player.getName();
    ProxiumNode server = player.getServer();  // 현재 접속 중인 서버
}

// 서버 노드 정보 조회
ProxiumNode node = proxium.getServer("Survival-1");
if (node != null) {
    String name = node.name();
    String host = node.host();
    int port = node.port();
}
```

---

## 7. 주의사항

> [!IMPORTANT]
> - **비동기 실행** — `.on()`, `.error()` 콜백은 **Netty I/O 스레드**에서 실행됩니다.
>   Bukkit API 호출 시 `Bukkit.getScheduler().runTask()`로 메인 스레드에 전환하세요.
> - **메인 스레드 블로킹 금지** — `asFuture().join()`을 메인 스레드에서 호출하면 서버가 멈출 수 있습니다.
> - **양방향 등록** — 요청 측과 응답 측 **모두** 동일한 채널을 사용해야 합니다.
> - **타입 등록 시점** — 가능한 서버 시작 시점(onEnable)에 `subscribe()`, `respond()` 등을 호출하여 타입을 등록하세요.

---

## 8. 전체 예제

### 예제 1: 크로스서버 경제 시스템

#### 데이터 클래스

```java
public record BalanceRequest(UUID uuid) {}
public record BalanceResponse(UUID uuid, double balance) {}
public record TransferRequest(UUID from, UUID to, double amount) {}
public record TransferResponse(boolean success, String message) {}
```

#### Survival-1 서버 (경제 데이터 보유)

```java
public class EconomyBridge {

    public void setup(Proxium proxium) {
        BridgeChannel channel = BridgeChannel.of("myplugin", "economy");

        proxium.respond(channel)
            .on(BalanceRequest.class, (sender, req) -> {
                double balance = getBalance(req.uuid());
                return new BalanceResponse(req.uuid(), balance);
            })
            .on(TransferRequest.class, (sender, req) -> {
                boolean ok = transfer(req.from(), req.to(), req.amount());
                return new TransferResponse(ok, ok ? "성공" : "잔액 부족");
            })
            .error(e -> getLogger().severe("Economy RPC 실패: " + e.getMessage()));
    }
}
```

#### Lobby 서버 (유저 인터페이스)

```java
public class LobbyCommands {

    private final Proxium proxium;
    private final BridgeChannel channel = BridgeChannel.of("myplugin", "economy");

    public void onBalanceCommand(Player player) {
        proxium.request("Survival-1", channel, new BalanceRequest(player.getUniqueId()))
            .on(BalanceResponse.class, (sender, res) -> {
                Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage("잔고: " + res.balance() + "원")
                );
            })
            .error(e -> player.sendMessage("조회 실패 — " + e.type()));
    }

    public void onTransferCommand(Player player, UUID target, double amount) {
        proxium.request("Survival-1", channel,
                new TransferRequest(player.getUniqueId(), target, amount),
                Duration.ofSeconds(10))
            .on(TransferResponse.class, (sender, res) -> {
                Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(res.success() ? "송금 완료!" : "실패: " + res.message())
                );
            })
            .error(e -> player.sendMessage("송금 요청 실패"));
    }
}
```

### 예제 2: 파티 초대 시스템 (Pub/Sub + RPC 혼합)

```java
// 데이터 클래스
public record PartyInvite(UUID inviter, UUID target, String partyName) {}
public record PartyAccept(UUID player, String partyName) {}
public record PartyResult(boolean success, String message) {}

public class PartyBridge {

    private final Proxium proxium;
    private final BridgeChannel channel = BridgeChannel.of("myplugin", "party");

    public void setup() {
        // Pub/Sub: 파티 초대 알림 수신 (모든 서버)
        proxium.subscribe(channel, PartyInvite.class, invite -> {
            Player target = Bukkit.getPlayer(invite.target());
            if (target != null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                    target.sendMessage("파티 초대: " + invite.partyName())
                );
            }
        });

        // RPC: 파티 수락 처리 (파티 데이터 서버)
        proxium.respond(channel)
            .on(PartyAccept.class, (sender, accept) -> {
                boolean ok = joinParty(accept.player(), accept.partyName());
                return new PartyResult(ok, ok ? "파티 참가 완료" : "파티를 찾을 수 없음");
            })
            .error(e -> getLogger().severe("Party RPC error: " + e.getMessage()));
    }

    // 초대 전송 (모든 서버에 브로드캐스트)
    public void invite(UUID inviter, UUID target, String partyName) {
        proxium.publish(channel, new PartyInvite(inviter, target, partyName));
    }

    // 수락 (파티 데이터 서버로 RPC)
    public void accept(Player player, String partyName) {
        proxium.request("Party-Server", channel, new PartyAccept(player.getUniqueId(), partyName))
            .on(PartyResult.class, (sender, result) -> {
                Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(result.message())
                );
            })
            .error(e -> player.sendMessage("파티 수락 실패: " + e.type()));
    }
}
```

### 예제 3: 서버 상태 모니터링

```java
public record ServerStatus(String serverName, int playerCount, double tps, long uptime) {}

// ─── 모든 백엔드 서버: 1분마다 상태 브로드캐스트 ───
BridgeChannel statusChannel = BridgeChannel.of("myplugin", "status");

Bukkit.getScheduler().runTaskTimerAsync(plugin, () -> {
    proxium.publish(statusChannel, new ServerStatus(
        proxium.getServer(),
        Bukkit.getOnlinePlayers().size(),
        Bukkit.getTPS()[0],
        ManagementFactory.getRuntimeMXBean().getUptime()
    ));
}, 0L, 1200L); // 60초 간격

// ─── 프록시 또는 모니터링 서버: 상태 수집 ───
Map<String, ServerStatus> statusMap = new ConcurrentHashMap<>();

proxium.subscribe(statusChannel, ServerStatus.class, status -> {
    statusMap.put(status.serverName(), status);
});
```

---

## 9. 구성 파일

`Config/Bridge/Proxium.yml` — 최초 실행 시 자동 생성됩니다.

```yaml
tls:
  enabled: true            # TLS 암호화 (자체 서명 인증서 자동 생성)

compression: SNAPPY        # 압축 방식: NONE / GZIP / SNAPPY / FAST_LZ

max-packet-size: 67108864  # 최대 패킷 크기 (bytes, 기본 64MB)

request-timeout: 5000      # RPC 기본 타임아웃 (ms)
```

| 항목 | 기본값 | 설명 |
|------|--------|------|
| `tls.enabled` | `true` | TLS 암호화 활성화 |
| `compression` | `SNAPPY` | 패킷 압축 알고리즘 |
| `max-packet-size` | `67108864` | 최대 패킷 크기 (bytes) |
| `request-timeout` | `5000` | `request()` 기본 타임아웃 (ms) |

---

## 10. API 레퍼런스

### Bridge (공통)

| 메서드 | 설명 |
|--------|------|
| `register(channel, types...)` | 채널에 데이터 클래스 바인딩 |
| `subscribe(channel, type, handler)` | 채널 구독 (타입별, 자동 등록) |
| `publish(channel, message)` | 메시지 브로드캐스트 |
| `unsubscribe(channel)` | 구독 취소 |
| `isConnected()` | 네트워크 연결 상태 확인 |
| `close()` | 브릿지 종료 |

### Proxium

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `isLoaded()` | `boolean` | 프로토콜 로드 여부 |
| `getServer()` | `String` | 로컬 서버 이름 (미연결 시 `null`) |
| `getServer(name)` | `ProxiumNode?` | 이름으로 서버 노드 조회 |
| `getPlayers()` | `Map<UUID, ProxyPlayer>` | 전체 플레이어 목록 |
| `getPlayer(uuid)` | `ProxyPlayer?` | UUID로 플레이어 조회 |
| `request(target, channel, payload, timeout)` | `RequestContext` | RPC 요청 (명시적 타임아웃) |
| `request(target, channel, payload)` | `RequestContext` | RPC 요청 (기본 타임아웃) |
| `getRequestTimeout()` | `Duration` | 구성된 기본 타임아웃 |
| `respond(channel)` | `ResponseContext` | RPC 응답 핸들러 등록기 |

### RequestContext — `request()` 반환

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `.on(Type.class, (sender, response) -> {})` | `RequestContext` | 응답 타입 핸들러 (체이닝) |
| `.error(e -> {})` | `RequestContext` | 에러 핸들러 |
| `.asFuture(Type.class)` | `CompletableFuture<T>` | Future 변환 |

### ResponseContext — `respond()` 반환

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `.on(Type.class, (sender, req) -> resp)` | `ResponseContext` | 요청 타입 핸들러 (체이닝) |
| `.error(e -> {})` | `ResponseContext` | 에러 핸들러 |

### BridgeChannel

| 메서드/상수 | 설명 |
|------------|------|
| `BridgeChannel.of("namespace", "key")` | 커스텀 채널 생성 |
| `BridgeChannel.of("namespace:key")` | 문자열에서 채널 파싱 |
| `BridgeChannel.INTERNAL` | 프레임워크 내부 채널 |
| `BridgeChannel.AUDIENCE` | 브로드캐스트 채널 |
| `toString()` | `"namespace:key"` 형태 문자열 |
| `toKey()` | Adventure `Key` 변환 |
