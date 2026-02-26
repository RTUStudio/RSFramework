# Storage 시스템

다양한 데이터베이스의 커넥션과 생명 주기를 통합 관리하는 스토리지 시스템.

## 모듈 구조

```
Storage/
├── Common/       Storage, StorageType, StorageLogger (공통 API)
├── MySQL/        MySQL 구현체
├── MariaDB/      MariaDB 구현체
├── PostgreSQL/   PostgreSQL 구현체
├── MongoDB/      MongoDB 구현체
├── SQLite/       SQLite 구현체
└── Json/         JSON 파일 구현체
```

## 작동 방식

1. **즉시 초기화** — `registerStorage` 호출 시 설정을 로드하고 바로 커넥션을 생성한다.
2. **변경 감지 리로드** — 설정 리로드 시 이전 값과 비교하여 변경된 커넥션만 재연결한다. 불필요한 DB 재연결 오버헤드를 방지한다.
3. **격리된 캐싱** — `Framework` 내부에서 `Map<String, StorageManager>` 형태로 플러그인별 스토리지를 완전히 격리하여 관리한다.

## 사용법

### 등록

```java
import kr.rtustudio.storage.StorageType;

registerStorage("UserData", StorageType.MYSQL);
```

`Config/Storage/MySQL.yml` 파일이 자동 생성되며, 관리자가 접속 정보를 입력한다.

### 접근

```java
import kr.rtustudio.storage.Storage;

Storage storage = getStorage("UserData");
if (storage != null && storage.isConnected()) {
    Object connection = storage.getConnection();
}
```

## 지원 타입

| 타입 | 분류 |
|-----|------|
| JSON, SQLite | 로컬 |
| MySQL, MariaDB, PostgreSQL | 관계형 DB |
| MongoDB | NoSQL |
