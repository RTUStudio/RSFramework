# RSFramework

Minecraft 플러그인 개발을 위한 멀티 플랫폼 프레임워크.  
Bukkit/Paper/Folia, BungeeCord, Velocity를 단일 코드베이스로 지원하며,  
Broker(Redis/ProtoWeaver), Storage(MySQL/MariaDB/MongoDB/SQLite/PostgreSQL/Json), LightDI, NMS 추상화 등을 제공합니다.

---

## 목차

- [프로젝트 구조](#프로젝트-구조)
- [모듈 개요](#모듈-개요)
- [빌드](#빌드)
- [상세 문서](#상세-문서)

---

## 프로젝트 구조

```
RSFramework/
├── Framework/
│   ├── API/          — RSPlugin, RSConfiguration, RSListener, Storage/Broker 설정 클래스
│   ├── Core/         — Framework 구현체, ModuleFactory, ProviderFactory
│   └── NMS/          — 1_20_R1 ~ 1_21_R7 NMS 구현체
├── Broker/
│   ├── Common/       — Broker, BrokerOptions, BrokerSerializer, BrokerRegistry
│   ├── Redisson/     — RedisBroker, Redisson, RedisConfig
│   └── ProtoWeaver/
│       ├── Common/API,Core  — 프로토콜 정의, Netty 서버
│       ├── Bukkit/API,Core  — Bukkit ProtoWeaver 구현체
│       ├── Bungee/API,Core  — BungeeCord ProtoWeaver 구현체
│       └── Velocity/API,Core — Velocity ProtoWeaver 구현체
├── Storage/
│   ├── Common/       — Storage 인터페이스, Result, StorageLogger
│   ├── MySQL/        — MySQLStorage, MySQLConfig
│   ├── MariaDB/      — MariaDBStorage, MariaDBConfig
│   ├── MongoDB/      — MongoDBStorage, MongoDBConfig
│   ├── SQLite/       — SQLiteStorage, SQLiteConfig
│   ├── PostgreSQL/   — PostgreSQLStorage, PostgreSQLConfig
│   └── Json/         — JsonStorage, JsonConfig
├── LightDI/          — 경량 DI 컨테이너
└── Platform/
    ├── Spigot, Paper, Folia
    ├── Bungee, Velocity
```

---

## 모듈 개요

| 모듈 | 패키지 | 설명 |
|------|--------|------|
| Framework:API | `kr.rtustudio.framework.bukkit.api` | RSPlugin, RSConfiguration, RSListener 등 핵심 API |
| Framework:Core | `kr.rtustudio.framework.bukkit.core` | Framework 구현체, 모듈/프로바이더 팩토리 |
| Framework:NMS | `kr.rtustudio.framework.bukkit.nms.*` | 버전별 NMS 추상화 (1.20.1 ~ 1.21.7) |
| Broker:Common | `kr.rtustudio.broker` | Broker 인터페이스, BrokerOptions(Fory), BrokerRegistry |
| Broker:Redisson | `kr.rtustudio.broker.redis` | Redis Pub/Sub 브로커 |
| Broker:ProtoWeaver | `kr.rtustudio.broker.protoweaver` | Netty 기반 서버간 바이너리 통신 |
| Storage:Common | `kr.rtustudio.storage` | Storage 인터페이스, Result enum, StorageLogger |
| Storage:MySQL | `kr.rtustudio.storage.mysql` | MySQL JSON 컬럼 기반 스토리지 |
| Storage:MariaDB | `kr.rtustudio.storage.mariadb` | MariaDB JSON 컬럼 기반 스토리지 |
| Storage:MongoDB | `kr.rtustudio.storage.mongodb` | MongoDB Document 스토리지 |
| Storage:SQLite | `kr.rtustudio.storage.sqlite` | SQLite 파일 기반 스토리지 |
| Storage:PostgreSQL | `kr.rtustudio.storage.postgresql` | PostgreSQL JSONB 스토리지 |
| Storage:Json | `kr.rtustudio.storage.json` | 로컬 JSON 파일 스토리지 |
| LightDI | `kr.rtustudio.cdi` | 경량 의존성 주입 컨테이너 |

---

## 빌드

```bash
# 전체 빌드
./gradlew build

# 코드 스타일 자동 수정
./gradlew spotlessApply

# 특정 모듈만 컴파일
./gradlew :Framework:API:compileJava
./gradlew :Storage:MySQL:compileJava
./gradlew :Broker:Redisson:compileJava
```

**Java 21** 이상 필요. Gradle Toolchain 자동 설정.

---

## 상세 문서

- [Framework (RSPlugin, RSConfiguration, Module, Provider)](docs/framework.md)
- [Broker (Redis, ProtoWeaver)](docs/broker.md)
- [Storage (MySQL, MariaDB, MongoDB, SQLite, PostgreSQL, Json)](docs/storage.md)
- [LightDI](docs/lightdi.md)
- [NMS 추상화](docs/nms.md)
