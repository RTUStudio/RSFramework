# Storage

`JsonObject` 기반의 통일된 인터페이스로 여러 데이터베이스를 추상화하는 모듈입니다.
모든 데이터베이스 조작은 `CompletableFuture`를 반환하여 비동기 처리를 기본으로 지원합니다.
플러그인은 코드를 변경하지 않고 `Config/Storage.yml` 설정만으로 데이터베이스 엔진을 교체할 수 있습니다.

---

## 1. Storage 인터페이스

`kr.rtustudio.storage.Storage`
모든 저장소 구현체가 구현하는 공통 인터페이스입니다.

```java
public interface Storage {

    // 새 문서/행 삽입
    CompletableFuture<Result> add(String table, JsonObject data);

    // 조건(find)에 맞는 문서를 업데이트 (data=null이면 삭제)
    CompletableFuture<Result> set(String table, JsonObject find, JsonObject data);

    // 조건(find)에 맞는 문서 목록 조회 (비어있으면 전체 조회)
    CompletableFuture<List<JsonObject>> get(String table, JsonObject find);

    // 연결 종료
    void close();

    enum Result {
        UPDATED(true, true),
        FAILED(false, false),
        UNCHANGED(true, false);

        boolean success;
        boolean changed;
    }
}
```

---

## 2. Framework (RSPlugin)에서 사용

### 2.1. 스토리지 등록 및 설정
`RSPlugin.load()` 단계에서 `registerStorage()`를 호출하여 스토리지를 등록합니다.

```java
@Override
protected void load() {
    // 기본 타입(JSON)으로 등록
    registerStorage("LocalData");

    // 기본 타입을 MySQL로 강제 지정
    registerStorage("RemoteData", StorageType.MYSQL);
}
```

위 코드는 `Config/Storage.yml`에 다음과 같은 설정을 자동 생성합니다.
(사용자가 `Storage.yml`의 값을 바꾸면 플러그인 코드 수정 없이 적용됩니다.)

```yaml
# Config/Storage.yml
LocalData: "JSON"
RemoteData: "MYSQL"
```

### 2.2. 데이터베이스별 상세 설정
각 DB 타입에 필요한 인증 정보 및 파일 경로는 `Config/Storage/` 하위 `.yml` 파일에서 관리합니다.

| 파일 (`Config/Storage/`) | 설정 항목 |
|-------------------------|-----------|
| `Json.yml` | `data-folder` |
| `SQLite.yml` | `file-path` |
| `MySQL.yml` | `host`, `port`, `database`, `username`, `password`, `table-prefix`, `use-arrow-operator` |
| `MariaDB.yml` | `host`, `port`, `database`, `username`, `password`, `table-prefix`, `use-arrow-operator` |
| `MongoDB.yml` | `host`, `port`, `database`, `username`, `password`, `collection-prefix` |
| `PostgreSQL.yml` | `host`, `port`, `database`, `username`, `password`, `table-prefix` |

#### 파일 저장 경로 (Json, SQLite)
상대 경로는 `plugins/<Plugin>/` 폴더 기준입니다. `/`로 시작하면 절대 경로로 인식합니다.

```yaml
# Config/Storage/SQLite.yml
file-path: "Data/SQLite.db"          # 상대 경로: plugins/MyPlugin/Data/SQLite.db

# Config/Storage/Json.yml
data-folder: "Data"                  # 상대 경로: plugins/MyPlugin/Data/
```

### 2.3. 데이터 조작 예시

```java
@Override
protected void enable() {
    Storage storage = getStorage("LocalData");

    // 조회 조건
    JsonObject find = new JsonObject();
    find.addProperty("uuid", player.getUniqueId().toString());

    // 1. 조회 (GET)
    storage.get("players", find).thenAccept(results -> {
        if (results.isEmpty()) {
            // 2. 추가 (ADD)
            JsonObject data = new JsonObject();
            data.addProperty("uuid", player.getUniqueId().toString());
            data.addProperty("level", 1);
            storage.add("players", data);
        } else {
            JsonObject record = results.get(0);
            int level = record.get("level").getAsInt();

            // 3. 업데이트 (SET)
            JsonObject update = new JsonObject();
            update.addProperty("level", level + 1);
            storage.set("players", find, update);
        }
    });

    // 4. 삭제 (SET with data=null)
    storage.set("players", find, null);
}
```

---

## 3. 구현체별 최적화 특징

RSFramework는 SQL 데이터베이스에서도 JSON을 완벽하게 다루기 위해 **최신 RDBMS의 네이티브 JSON 문법**을 적극 활용합니다.
또한 최근 대규모 업데이트를 통해 **완벽한 Thread-safe 및 커넥션 누수 방지(Try-with-resources)**가 적용되었습니다.

| 엔진 | 저장 방식 | 네이티브 JSON 쿼리 최적화 내역 |
|------|-----------|--------------------------------|
| **MySQL** | `JSON` 컬럼 | `JSON_SET`, `JSON_EXTRACT`, `->>` 연산자, `CAST(? AS JSON)` |
| **MariaDB** | `JSON` 컬럼 | MySQL과 동일한 구조 및 연산자 사용 |
| **PostgreSQL** | `JSONB` 컬럼 | `jsonb_build_object`, `->` 연산자, `::jsonb` 캐스팅 지원 |
| **SQLite** | `JSON` 컬럼 (v3.45+) | `json()`, `json_extract`, `json_patch`, `->>` 연산자 활용. WAL 모드 활성화 |
| **MongoDB** | BSON | 네이티브 Document 쿼리 및 Upsert 사용 |
| **Json** | `.json` 파일 | 로컬 파일 기반. `ConcurrentHashMap`과 `synchronized` 락으로 멀티스레드 완벽 지원 |

---

## 4. StorageLogger

모든 구현체에서 사용하는 공통 로깅 유틸리티입니다 (`kr.rtustudio.storage.StorageLogger`).
실제 쿼리 내용과 파라미터가 치환된 완벽한 Debug Query 문자열을 출력합니다.
로그 레벨은 `DEBUG`이므로 기본적으로 콘솔에 출력되지 않으나, SLF4J 설정에서 `kr.rtustudio.storage` 패키지를 `DEBUG` 레벨로 변경하면 쿼리 흐름을 완벽히 추적할 수 있습니다.
