# RSFramework Storage 시스템

플러그인 개발 중 파편화되기 쉬운 데이터베이스 커넥션 생성, 유지 보수, Connection Pool 관리, 생명 주기(Lifecycle)를 중앙에서 완전히 통제하는 **통합 스토리지(Storage) 시스템**입니다.

모든 데이터베이스를 **JSON 문서 기반 API** (`add` / `set` / `get`)로 통일하여, 플러그인 개발자가 SQL이나 MongoDB 드라이버에 의존하지 않고 동일한 인터페이스로 데이터를 관리할 수 있습니다.

---

## 1. 지원하는 데이터베이스 (StorageType)

| 스토리지 타입 | 분류 | 특징 |
|-------------|------|------|
| **JSON** | 로컬 시스템 | 로컬 `.json` 파일 기반 저장 |
| **SQLITE** | 로컬 시스템 | HikariCP 기반 로컬 `.db` 파일 |
| **MYSQL** | 관계형 DB | mysql-connector 드라이버 |
| **MARIADB** | 관계형 DB | mariadb-java-client 드라이버 |
| **POSTGRESQL** | 관계형 DB | PostgreSQL 드라이버 |
| **MONGODB** | NoSQL | MongoDB 비동기 문서 클러스터 |

> [!TIP]
> 어떤 `StorageType`을 선택하든 플러그인 코드는 **동일한 `Storage` API**를 사용합니다.
> 서버 관리자가 설정 파일에서 타입만 변경하면 자동으로 해당 드라이버로 전환됩니다.

---

## 2. 아키텍처

```text
Storage/
├── Common/       Storage 핵심 인터페이스, StorageType, JSON 빌더, StorageLogger
├── MySQL/        MySQL 구현체
├── MariaDB/      MariaDB 구현체
├── PostgreSQL/   PostgreSQL 구현체
├── MongoDB/      MongoDB 구현체
├── SQLite/       SQLite 구현체
└── Json/         로컬 JSON 파일 구현체
```

### HikariCP 풀링 내장
관계형 DB를 `registerStorage()`로 선언하면 HikariCP 커넥션 풀이 자동으로 생성됩니다.

### 핫 리로드 보호
`/rsf reload` 시 DB 접속 정보가 변경되지 않았으면 재연결하지 않습니다. 변경된 스토리지만 감지하여 핫 리로드합니다.

---

## 3. Storage API

### 핵심 인터페이스

```java
public interface Storage {

    // 문서 삽입
    CompletableFuture<Result> add(JsonObject data);
    CompletableFuture<Result> add(JSON data);

    // 문서 수정 (find 조건으로 찾아서 data로 갱신)
    CompletableFuture<Result> set(JsonObject find, JsonObject data);
    CompletableFuture<Result> set(JSON find, JSON data);

    // 문서 조회 (find 조건에 맞는 문서 목록 반환)
    CompletableFuture<List<JsonObject>> get(JsonObject find);
    CompletableFuture<List<JsonObject>> get(JSON find);

    void close();
}
```

### Result (결과 상태)

| 상태 | `success` | `changed` | 의미 |
|------|-----------|-----------|------|
| `UPDATED` | ✅ | ✅ | 성공적으로 변경됨 |
| `UNCHANGED` | ✅ | ❌ | 성공했지만 변경 사항 없음 |
| `FAILED` | ❌ | ❌ | 실패 |

### JSON 빌더 유틸리티

`JSON` 클래스는 `JsonObject`를 빌더 패턴으로 구성합니다.

```java
import kr.rtustudio.storage.JSON;

// 여러 가지 생성 방법
JSON data = JSON.of("name", "IPECTER");
JSON data = JSON.of("age", 25);
JSON data = JSON.of("active", true);

// 체이닝으로 여러 필드 추가
JSON player = JSON.of("uuid", uuid.toString())
    .append("name", "IPECTER")
    .append("coins", 1000)
    .append("level", 5);

// JsonObject 변환
JsonObject json = player.get();
```

---

## 4. 사용 가이드

### 4.1. 스토리지 등록

```java
@Override
protected void enable() {
    // 타입 지정 등록
    registerStorage("PlayerData", StorageType.MARIADB);
    registerStorage("Settings", StorageType.SQLITE);

    // 기본 타입(JSON) 등록
    registerStorage("Cache");
}
```

등록 직후, `Config/Storage/PlayerData.yml`이 자동 생성됩니다:
```yaml
Host: '127.0.0.1'
Port: 3306
Username: 'root'
Password: 'password123'
Database: 'minecraft_server'
```

### 4.2. 설정 파일에서 StorageType 결정

```java
// 문자열 → StorageType 변환 (대소문자 무시, 매칭 실패 시 JSON)
StorageType type = StorageType.fromString("mariadb");  // → MARIADB
StorageType type2 = StorageType.fromString("MySQL");    // → MYSQL
StorageType type3 = StorageType.fromString("unknown");  // → JSON (기본값)

// 설정 파일의 값으로 등록
String dbType = getConfig().getString("database-type", "sqlite");
registerStorage("PlayerData", StorageType.fromString(dbType));
```

### 4.3. Storage 인스턴스 접근

```java
Storage storage = getStorage("PlayerData");
```

---

## 5. 실전 예제

### 예제 1: 플레이어 데이터 CRUD

```java
public class PlayerStorage {

    private final Storage storage;

    public PlayerStorage(RSPlugin plugin) {
        this.storage = plugin.getStorage("PlayerData");
    }

    // ─── CREATE ───
    public void createPlayer(UUID uuid, String name) {
        JSON data = JSON.of("uuid", uuid.toString())
            .append("name", name)
            .append("coins", 0)
            .append("level", 1);

        storage.add(data).thenAccept(result -> {
            if (result.isSuccess()) {
                System.out.println("플레이어 생성 완료");
            }
        });
    }

    // ─── READ ───
    public void getPlayer(UUID uuid) {
        JSON find = JSON.of("uuid", uuid.toString());

        storage.get(find).thenAccept(results -> {
            if (results.isEmpty()) {
                System.out.println("플레이어를 찾을 수 없음");
                return;
            }
            JsonObject player = results.get(0);
            int coins = player.get("coins").getAsInt();
            System.out.println("코인: " + coins);
        });
    }

    // ─── UPDATE ───
    public void setCoins(UUID uuid, int newCoins) {
        JSON find = JSON.of("uuid", uuid.toString());
        JSON data = JSON.of("uuid", uuid.toString())
            .append("coins", newCoins);

        storage.set(find, data).thenAccept(result -> {
            if (result.isChanged()) {
                System.out.println("코인 업데이트 완료");
            }
        });
    }

    // ─── 레벨 변경 ───
    public void setLevel(UUID uuid, int level) {
        JSON find = JSON.of("uuid", uuid.toString());
        JSON data = JSON.of("uuid", uuid.toString())
            .append("level", level);

        storage.set(find, data).thenAccept(result -> {
            switch (result) {
                case UPDATED   -> System.out.println("레벨 변경됨");
                case UNCHANGED -> System.out.println("이미 같은 레벨");
                case FAILED    -> System.out.println("변경 실패");
            }
        });
    }
}
```

### 예제 2: 길드 시스템

```java
public class GuildStorage {

    private final Storage storage;

    public GuildStorage(RSPlugin plugin) {
        this.storage = plugin.getStorage("GuildData");
    }

    // 길드 생성
    public CompletableFuture<Storage.Result> createGuild(String name, UUID leader) {
        JSON data = JSON.of("name", name)
            .append("leader", leader.toString())
            .append("level", 1)
            .append("createdAt", System.currentTimeMillis());

        return storage.add(data);
    }

    // 이름으로 길드 조회
    public CompletableFuture<List<JsonObject>> findGuild(String name) {
        return storage.get(JSON.of("name", name));
    }

    // 길드 레벨 업
    public CompletableFuture<Storage.Result> levelUp(String guildName, int newLevel) {
        JSON find = JSON.of("name", guildName);
        JSON data = JSON.of("name", guildName)
            .append("level", newLevel);

        return storage.set(find, data);
    }
}
```

### 예제 3: 비동기 + Bukkit 메인 스레드 전환

```java
public void showBalance(Player player) {
    JSON find = JSON.of("uuid", player.getUniqueId().toString());

    storage.get(find).thenAccept(results -> {
        // ⚠️ 이 콜백은 비동기 스레드에서 실행됨
        // Bukkit API 호출 시 메인 스레드로 전환 필요
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (results.isEmpty()) {
                player.sendMessage("데이터가 없습니다.");
                return;
            }
            int coins = results.get(0).get("coins").getAsInt();
            player.sendMessage("보유 코인: " + coins);
        });
    });
}
```

---

## 6. 주의사항

> [!IMPORTANT]
> - **비동기 반환** — `add()`, `set()`, `get()` 모두 `CompletableFuture`를 반환합니다.
>   Bukkit API 호출 시 `Bukkit.getScheduler().runTask()`로 메인 스레드에 전환하세요.
> - **메인 스레드 블로킹 금지** — `.join()` 또는 `.get()`을 메인 스레드에서 호출하면 서버가 멈출 수 있습니다.
> - **DB 독립적** — 동일한 코드로 JSON, SQLite, MySQL, MongoDB 등 어떤 백엔드든 사용 가능합니다.

---

## 7. API 레퍼런스

### Storage

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `add(JsonObject data)` | `CompletableFuture<Result>` | 문서 삽입 |
| `add(JSON data)` | `CompletableFuture<Result>` | 문서 삽입 (빌더) |
| `set(JsonObject find, JsonObject data)` | `CompletableFuture<Result>` | 조건 검색 후 문서 갱신 |
| `set(JSON find, JSON data)` | `CompletableFuture<Result>` | 조건 검색 후 문서 갱신 (빌더) |
| `get(JsonObject find)` | `CompletableFuture<List<JsonObject>>` | 조건 검색 후 문서 목록 반환 |
| `get(JSON find)` | `CompletableFuture<List<JsonObject>>` | 조건 검색 후 문서 목록 반환 (빌더) |
| `close()` | `void` | 스토리지 종료 |

### JSON (빌더)

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `JSON.of()` | `JSON` | 빈 빌더 생성 |
| `JSON.of(key, value)` | `JSON` | 초기 필드를 가진 빌더 생성 |
| `append(key, value)` | `JSON` | 필드 추가 (체이닝) |
| `get()` | `JsonObject` | `JsonObject`로 변환 |

### StorageType

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `StorageType.fromString(type)` | `StorageType` | 문자열→타입 변환 (대소문자 무시, 실패 시 JSON) |

### RSPlugin (스토리지 관련)

| 메서드 | 설명 |
|--------|------|
| `registerStorage(name, type)` | 스토리지 등록 |
| `registerStorage(name)` | JSON 타입으로 등록 |
| `getStorage(name)` | 등록된 스토리지 인스턴스 반환 |
