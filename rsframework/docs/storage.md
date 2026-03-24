# RSFramework Storage 시스템

모든 데이터베이스를 **JSON 문서 기반 API** (`add` / `set` / `get`)로 통일하는 통합 스토리지 시스템입니다.

---

## 1. 지원 데이터베이스

| 타입 | 분류 | 특징 |
|------|------|------|
| **JSON** | 로컬 | `.json` 파일 기반 |
| **SQLITE** | 로컬 | HikariCP 기반 `.db` 파일 |
| **MYSQL** | 관계형 DB | mysql-connector |
| **MARIADB** | 관계형 DB | mariadb-java-client |
| **POSTGRESQL** | 관계형 DB | PostgreSQL 드라이버 |
| **MONGODB** | NoSQL | 비동기 문서 클러스터 |

> [!TIP]
> 어떤 `StorageType`을 선택하든 **동일한 `Storage` API**를 사용합니다.
> 설정 파일에서 타입만 변경하면 드라이버가 자동 전환됩니다.

---

## 2. Storage API

### 핵심 인터페이스

```java
public interface Storage {
    CompletableFuture<Result> add(JSON data);       // 삽입
    CompletableFuture<Result> set(JSON find, JSON data);  // 수정
    CompletableFuture<List<JsonObject>> get(JSON find);    // 조회
    void close();
}
```

### Result (결과 상태)

| 상태 | `success` | `changed` | 의미 |
|------|-----------|-----------|------|
| `UPDATED` | ✅ | ✅ | 성공적으로 변경됨 |
| `UNCHANGED` | ✅ | ❌ | 성공했지만 변경 사항 없음 |
| `FAILED` | ❌ | ❌ | 실패 |

### JSON 빌더

```java
import kr.rtustudio.storage.JSON;

JSON player = JSON.of("uuid", uuid.toString())
    .append("name", "IPECTER")
    .append("coins", 1000)
    .append("level", 5);

JsonObject json = player.get(); // JsonObject 변환
```

---

## 3. 사용 가이드

### 3.1. 등록

```java
@Override
protected void enable() {
    registerStorage("PlayerData", StorageType.MARIADB);
    registerStorage("Settings", StorageType.SQLITE);
    registerStorage("Cache"); // 기본 JSON
}
```

등록 시 `Config/Storage/PlayerData.yml`이 자동 생성됩니다:
```yaml
Host: '127.0.0.1'
Port: 3306
Username: 'root'
Password: 'password123'
Database: 'minecraft_server'
```

### 3.2. 설정 파일에서 타입 결정

```java
String dbType = getConfig().getString("database-type", "sqlite");
registerStorage("PlayerData", StorageType.fromString(dbType));
```

### 3.3. 인스턴스 접근

```java
Storage storage = getStorage("PlayerData");
```

---

## 4. 실전 예제

### 플레이어 데이터 CRUD

```java
public class PlayerStorage {

    private final Storage storage;

    public PlayerStorage(RSPlugin plugin) {
        this.storage = plugin.getStorage("PlayerData");
    }

    // CREATE
    public void createPlayer(UUID uuid, String name) {
        storage.add(JSON.of("uuid", uuid.toString())
            .append("name", name)
            .append("coins", 0)
            .append("level", 1)
        ).thenAccept(result -> {
            if (result.isSuccess()) System.out.println("플레이어 생성 완료");
        });
    }

    // READ
    public void getPlayer(UUID uuid) {
        storage.get(JSON.of("uuid", uuid.toString())).thenAccept(results -> {
            if (!results.isEmpty()) {
                int coins = results.get(0).get("coins").getAsInt();
                System.out.println("코인: " + coins);
            }
        });
    }

    // UPDATE
    public void setCoins(UUID uuid, int newCoins) {
        JSON find = JSON.of("uuid", uuid.toString());
        JSON data = JSON.of("uuid", uuid.toString()).append("coins", newCoins);

        storage.set(find, data).thenAccept(result -> {
            if (result.isChanged()) System.out.println("코인 업데이트 완료");
        });
    }
}
```

### Bukkit 메인 스레드 전환

```java
public void showBalance(Player player) {
    storage.get(JSON.of("uuid", player.getUniqueId().toString())).thenAccept(results -> {
        // ⚠️ 비동기 스레드에서 실행됨 — Bukkit API 호출 시 전환 필요
        CraftScheduler.sync(plugin, task -> {
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

## 5. 주의사항

> [!IMPORTANT]
> - **비동기 반환** — `add()`, `set()`, `get()` 모두 `CompletableFuture`를 반환합니다.
>   Bukkit API 호출 시 `CraftScheduler.sync()`로 전환하세요.
> - **메인 스레드 블로킹 금지** — `.join()` 또는 `.get()`을 메인 스레드에서 호출하면 서버가 멈출 수 있습니다.
> - **DB 독립적** — 동일한 코드로 JSON, SQLite, MySQL, MongoDB 등 어떤 백엔드든 사용 가능합니다.
> - **HikariCP 내장** — 관계형 DB 등록 시 커넥션 풀이 자동 생성됩니다.
> - **핫 리로드** — `/rsf reload` 시 변경된 스토리지만 감지하여 재연결합니다.

---

## 6. API 레퍼런스

### Storage

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `add(JSON data)` | `CompletableFuture<Result>` | 문서 삽입 |
| `set(JSON find, JSON data)` | `CompletableFuture<Result>` | 조건 검색 후 갱신 |
| `get(JSON find)` | `CompletableFuture<List<JsonObject>>` | 조건 검색 후 조회 |
| `close()` | `void` | 스토리지 종료 |

### JSON (빌더)

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `JSON.of()` | `JSON` | 빈 빌더 |
| `JSON.of(key, value)` | `JSON` | 초기 필드 지정 |
| `append(key, value)` | `JSON` | 필드 추가 (체이닝) |
| `get()` | `JsonObject` | 변환 |

### RSPlugin (스토리지 관련)

| 메서드 | 설명 |
|--------|------|
| `registerStorage(name, type)` | 스토리지 등록 |
| `registerStorage(name)` | JSON 타입으로 등록 |
| `getStorage(name)` | 등록된 인스턴스 반환 |
