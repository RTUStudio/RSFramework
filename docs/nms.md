# 🛠️ NMS (Net Minecraft Server) 및 플랫폼 호환성

RSFramework는 단일 플러그인 코드로 여러 Minecraft 버전을 지원하고, 다양한 서버 환경(Spigot, Paper, Folia)에서 완벽히 작동하도록 추상화 레이어를 제공합니다.

## 1. NMS 버저닝 아키텍처

Minecraft 서버의 고유 내부 코드(NMS)에 접근하기 위해 컴파일 시 다중 버전 모듈을 사용합니다.

```text
Framework/
└── NMS/
    ├── Common/       (버전 독립적인 NMS 인터페이스 정의)
    ├── v1_20_R4/     (1.20.6 NMS 구현체)
    ├── v1_21_R1/     (1.21 NMS 구현체)
    └── v1_21_R3/     (1.21.4 NMS 구현체)
```
- 패키지 경로는 `kr.rtustudio.protoweaver.bukkit.nms` 하위에 위치합니다.
- 코어 모듈 구동 시 서버의 버전(`Bukkit.getServer().getClass().getPackage().getName()`)을 파악하여 알맞은 NMS 구현체를 `LightDI` 컨테이너에 동적으로 바인딩합니다.

## 2. 서버 소프트웨어 환경 호환성

RSFramework는 `Paper`와 멀티스레딩 포크인 `Folia` 환경을 네이티브로 지원합니다.

### Folia 대응 스케줄링 (`CraftScheduler`)
Bukkit의 기본 `BukkitRunnable`은 Folia에서 작동하지 않습니다. 
프레임워크의 `CraftScheduler`는 런타임 환경이 Folia인지 식별하고, 내부적으로 `RegionScheduler` 및 `GlobalRegionScheduler`를 사용하여 동기/비동기 작업을 안전하게 위임합니다.

### Paper 네이티브 API 지향 (`MinecraftVersion`)
`kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion` 유틸리티를 통해 서버가 Paper 기반인지 식별합니다. 
Paper 전용 API(예: 비동기 텔레포트 `player.teleportAsync`, 네이티브 Adventure Audience 객체)가 사용 가능할 경우, Spigot 레거시 메서드 대신 Paper API를 우선적으로 호출하도록 최적화되어 있습니다.
