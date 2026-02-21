# 🏗️ 시스템 아키텍처 (Architecture)

RSFramework는 확장 가능하고 유지보수가 용이한 모듈화된 아키텍처를 채택하고 있습니다.
프레임워크는 여러 하위 프로젝트로 나뉘어져 있으며, 목적에 따라 명확히 분리된 책임을 가집니다.

## 📂 프로젝트 구조

```text
RSFramework/
├── LightDI/             # 자체 제작 경량 의존성 주입(DI) 컨테이너
├── Broker/              # Pub/Sub 및 메시징 시스템
│   ├── Common/          # 공통 Broker 인터페이스
│   ├── Redisson/        # Redis 구현체
│   └── ProtoWeaver/     # Bungee/Velocity 프록시 통신 구현체
├── Framework/           # 프레임워크 핵심
│   ├── API/             # 플러그인 개발자가 사용하는 공개 API
│   ├── Core/            # 프레임워크 내부 엔진 및 매니저
│   └── NMS/             # 버전별 Minecraft 내부 서버 로직 분리
└── Platform/            # 서버 플랫폼별 진입점
    └── Spigot, Paper, Folia, Bungee, Velocity
```

## 🧩 모듈 및 프로바이더 시스템 (Module & Provider)

프레임워크 내부의 다양한 기능과 서비스를 효과적으로 제공하기 위해 `Module`과 `Provider` 패턴을 사용합니다.
모든 팩토리는 `ConcurrentHashMap`을 기반으로 설계되어 스레드 안전성을 보장하며, 제네릭 타입 캐스팅(`unchecked cast`) 오류 없이 안전하게 구현되었습니다.

### 1. Module
플러그인의 거대한 기능 단위(주제)를 나타냅니다.
- **예시**: `ThemeModule`, `CommandModule` 등
- **사용법**: `framework.getModule(ThemeModule.class)`

### 2. Provider
특정 데이터를 제공하거나 계산을 수행하는 단일 목적의 서비스입니다.
- **예시**: `VanillaNameProvider` (플레이어 이름/UUID 조회)
- **사용법**: `framework.getProvider(NameProvider.class)`

### 등록 및 생명주기
모듈과 프로바이더의 초기화 및 등록은 `Framework` 구동 시 코어 모듈(Core)의 `ModuleFactory` 및 `ProviderFactory`를 통해 이루어집니다. 외부 플러그인은 읽기 전용으로 인스턴스에 접근할 수 있습니다.
