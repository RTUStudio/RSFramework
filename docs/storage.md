# 💾 Storage 시스템

다양한 데이터베이스(RDBMS, NoSQL, 플랫 파일)의 커넥션과 생명 주기를 일관된 인터페이스로 관리하는 통합 스토리지 시스템입니다.

## 🏗️ 구조

- **API (`Storage`, `StorageType`)**: 플러그인 개발자가 사용하는 접근 인터페이스. `Json`, `MySQL`, `MongoDB` 등 명칭으로 구분합니다.
- **Core (`StorageManager`)**: 플러그인별 DB 커넥션 캐싱 및 생명 주기를 관리합니다.
- **Configuration (`StorageConfiguration`)**: `Config/Storage.yml` 및 하위 폴더의 연결 설정을 로드하고, 변경 사항을 추적합니다.

## ✨ 작동 방식 (최적화)

1. **지연 로딩 없는 초기화**: 스토리지 최초 등록 시(`registerStorage`), 해당하는 설정 객체를 일괄 로드하여 바로 인스턴스를 생성합니다.
2. **리로드 최적화 (`isChanged`)**: 설정 파일(`yml`)을 다시 로드할 때, 이전 필드 값과 새로운 값을 비교하여 **변경된 사항이 있거나 아직 연결되지 않은 스토리지에 한해서만** 기존 커넥션을 닫고 다시 맺습니다. 이를 통해 불필요한 DB 재연결 오버헤드를 막습니다.
3. **격리된 캐싱**: Core의 `Framework` 객체 내부에서 `Map<String, StorageManager>` 형태로 각 플러그인(RSPlugin)별 스토리지를 완전히 격리하여 관리합니다.

## 💻 사용 예시

### 1. 스토리지 등록
플러그인의 `onPluginEnable` 등에서 사용할 스토리지를 등록합니다.

```java
import kr.rtustudio.storage.StorageType;

// "UserData"라는 이름으로 MySQL 스토리지 등록
registerStorage("UserData", StorageType.MYSQL);
```

이후 `Config/Storage/MySQL.yml` 파일이 자동 생성되며, 관리자는 이곳에 접속 정보를 입력합니다.

### 2. 스토리지 접근
```java
import kr.rtustudio.storage.Storage;

Storage storage = getStorage("UserData");
if (storage != null && storage.isConnected()) {
    // DataSource(HikariCP 등) 또는 Connection 객체 접근
    Object connection = storage.getConnection();
}
```

### 지원되는 StorageType
- `JSON`, `SQLITE` (로컬)
- `MYSQL`, `MARIADB`, `POSTGRESQL` (관계형)
- `MONGODB` (NoSQL)
