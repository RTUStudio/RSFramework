# Bridge 시스템

서버 간 Pub/Sub 메시징을 위한 통합 브로커 시스템.

## 아키텍처

`kr.rtustudio.bridge.Bridge` 인터페이스로 추상화. 구현체와 무관하게 동일한 코드 패턴을 사용한다.

### 모듈 구조

```
Bridge/
├── Common/         Bridge, BridgeChannel, BridgeOptions, BridgeRegistry
├── Redisson/       Redis 구현체 (분산 락 지원)
└── Proxium/
    ├── Common/API  Proxium 인터페이스, Protocol, Connection, ProxyPlayer 등
    ├── Common/Core AbstractProxium, ProxiumServer, ProxiumProxy, SimpleConfiguration
    ├── Bukkit/     BukkitProxium
    ├── Bungee/     BungeeProxium
    └── Velocity/   VelocityProxium
```

### Redisson (Redis)

- Redisson 클라이언트 기반
- 분산 락(Distributed Lock) 지원: `withLock`, `tryLockOnce`

### Proxium (자체 프록시 통신)

- Netty 기반 커스텀 프로토콜
- BungeeCord / Velocity ↔ 백엔드 서버 간 TLS 보안 채널
- 프록시 네트워크 플레이어/서버 정보 접근

> **직렬화**: 두 구현체 모두 Fory를 사용한 고성능 바이너리 직렬화를 수행한다.

### Proxium 클래스 계층

```
AbstractProxium (Bridge 구현, register/subscribe/dispatchPacket)
├── ProxiumServer (서버 측 abstract) → BukkitProxium
└── ProxiumProxy  (프록시 측 abstract, 커넥션 매니저 포함) → BungeeProxium, VelocityProxium
```

### Proxium 주요 API 타입

| 클래스 | 패키지 | 설명 |
|-------|--------|------|
| `Proxium` | `api` | Bridge 확장, 연결 상태·플레이어·서버명 접근 |
| `Protocol` | `api.protocol` | 채널별 프로토콜 정의 (패킷 등록, 핸들러, 압축) |
| `Connection` | `api.netty` | 네트워크 연결 추상화 (프로토콜 업그레이드, 패킷 전송) |
| `RegisteredServer` | `api.proxy` | 프록시에 등록된 백엔드 서버 정보 |
| `ProxyConnector` | `api.proxy` | 프록시 → 서버 연결 클라이언트 |
| `ProxyPlayer` | `api.proxy` | 프록시 네트워크 플레이어 정보 |

## 사용법

```java
import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;

Bridge bridge = framework.getBridge(Proxium.class); // 또는 Redis.class
BridgeChannel channel = BridgeChannel.of("rsf", "test");

// 1. 채널 및 패킷 등록
bridge.register(channel, BuyPacket.class, SellPacket.class);

// 2. 패킷 수신 구독
bridge.subscribe(channel, packet -> {
    if (packet instanceof BuyPacket buy) {
        System.out.println(buy.getPlayerName() + "님이 구매했습니다.");
    }
});

// 3. 패킷 전송
bridge.publish(channel, new BuyPacket("ipecter", 500));
```

## BridgeOptions

채널별 패킷 등록 및 직렬화를 관리하는 공통 컴포넌트. Fory를 통해 동일한 직렬화 파이프라인을 사용하므로 구현체 차이를 신경 쓸 필요가 없다.

## Proxium 설정

`Config/Bridge/Proxium.yml`에서 TLS, 압축, 최대 패킷 크기, 서버 폴링 주기 등을 관리한다. `ProxiumSettings`(Proxium Core)가 `SimpleConfiguration`으로 로드한다.
