# 📡 Bridge 시스템

프록시 환경(BungeeCord, Velocity)이나 분산 서버 환경에서 서버 간 메시지를 주고받기 위한 통일된 Pub/Sub 브로커 시스템입니다.

## 🌟 통합 아키텍처

브로커 시스템은 구현체(Redis, Proxium)와 관계없이 `kr.rtustudio.bridge.Bridge`라는 단일 인터페이스로 추상화되어 있습니다.

### 1. Redisson (Redis)

- `Redisson` 클라이언트를 기반으로 구현된 브로커.
- 분산 락(Distributed Lock) 등 Redis 고유 기능을 추가로 지원합니다.

### 2. Proxium (자체 프록시 통신)

- Netty 기반의 커스텀 프록시 통신 프레임워크.
- BungeeCord / Velocity 프록시와 백엔드 서버 간 TLS 보안 채널을 통해 직접 통신합니다.
- 프록시 네트워크 플레이어/서버 정보 접근 등 고유 기능을 지원합니다.

> **직렬화**: 두 구현체 모두 `Fory`를 사용하여 동일한 고성능 바이너리 직렬화를 수행합니다.

#### 클래스 계층

- `AbstractProxium` — Bridge 구현, 공통 로직 (register, subscribe, dispatchPacket)
  - `ProxiumServer` — 서버 측 abstract → `BukkitProxium` extends
  - `ProxiumProxy` — 프록시 측 abstract, 커넥션 매니저 포함 → `BungeeProxium`, `VelocityProxium` extends

#### 주요 API 타입

| 클래스 | 패키지 | 설명 |
|-------|--------|------|
| `Proxium` | `api` | Bridge를 확장한 Proxium 인터페이스 (연결 상태, 플레이어, 서버명 등) |
| `Protocol` | `api.protocol` | 채널별 프로토콜 정의 (패킷 등록, 핸들러, 압축 등) |
| `Connection` | `api.netty` | 네트워크 연결 추상화 (프로토콜 업그레이드, 패킷 전송 등) |
| `RegisteredServer` | `api.proxy` | 프록시에 등록된 백엔드 서버 정보 |
| `ProxyConnector` | `api.proxy` | 프록시 → 서버 연결 클라이언트 |
| `ProxyPlayer` | `api.proxy` | 프록시 네트워크 플레이어 정보 |

## 🛠️ 공통 사용 패턴

어떤 Bridge를 사용하더라도 코드 패턴은 완벽히 동일합니다. 채널은 `BridgeChannel` 객체를 사용하여 정의하며 `네임스페이스`와 `키`로 구성됩니다.

```java
import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.Proxium;

Bridge bridge = framework.getBridge(Proxium.class); // 또는 Redis.class
BridgeChannel channel = BridgeChannel.of("rsf", "test");

// 1. 채널 및 패킷 등록
bridge.register(channel, BuyPacket.class, SellPacket.class);

// 2. 패킷 수신 구독 (Subscribe)
bridge.subscribe(channel, packet -> {
    if (packet instanceof BuyPacket buy) {
        System.out.println(buy.getPlayerName() + "님이 구매했습니다.");
    }
});

// 3. 패킷 전송 (Publish)
bridge.publish(channel, new BuyPacket("ipecter", 500));
```

## 🔌 BridgeOptions

`BridgeOptions`는 채널별 패킷 등록 및 직렬화를 관리하는 공통 컴포넌트입니다. 두 구현체 모두 `Fory`를 통해 동일한 직렬화 파이프라인을 사용하므로, 개발자는 구현체 차이를 신경 쓸 필요 없이 동일한 패킷 클래스를 사용할 수 있습니다.
