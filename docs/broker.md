# 📡 Bridge 시스템

프록시 환경(BungeeCord, Velocity)이나 분산 서버 환경에서 서버 간 메시지를 주고받기 위한 통일된 Pub/Sub 브로커 시스템입니다.

## 🌟 통합 아키텍처
브로커 시스템은 구현체(Redis, ProtoWeaver)와 관계없이 `kr.rtustudio.bridge.Bridge`라는 단일 인터페이스로 추상화되어 있습니다.

### 1. Redisson (Redis)
- `Redisson` 클라이언트를 기반으로 구현된 브로커.
- **Serializer**: 내부적으로 `Gson`의 `TypeAdapter<?>`를 사용하여 패킷을 직렬화합니다.

### 2. ProtoWeaver (자체 프록시 통신)
- BungeeCord / Velocity 플러그인 메시징 채널을 활용하는 커스텀 프록시 통신 프레임워크.
- **Serializer**: `BridgeSerializer`를 자체 `Fory Serializer`로 확장(Bridge)하여 사용합니다.

## 🛠️ 공통 사용 패턴

어떤 Bridge를 사용하더라도 코드 패턴은 완벽히 동일합니다. 채널은 `네임스페이스:키` (예: `rsf:test`) 형식으로 작성합니다.

```java
import kr.rtustudio.bridge.Bridge;
import kr.rtustudio.bridge.BridgeSerializer;

Bridge bridge = framework.getBridge(ProtoWeaver.class); // 또는 RedisBridge.class

// 1. 채널 및 패킷 등록
bridge.register("rsf:test", registrar -> {
    registrar.register(BuyPacket.class); // 기본 직렬화
    registrar.register(SellPacket.class, new SellPacketSerializer()); // 커스텀 직렬화기
});

// 2. 패킷 수신 구독 (Subscribe)
bridge.subscribe("rsf:test", packet -> {
    if (packet instanceof BuyPacket buy) {
        System.out.println(buy.getPlayerName() + "님이 구매했습니다.");
    }
});

// 3. 패킷 전송 (Publish)
bridge.publish("rsf:test", new BuyPacket("ipecter", 500));
```

## 🔌 BridgeSerializer 브릿지(Bridge)
플러그인 개발자는 브로커 종류에 구애받지 않고 통합된 `BridgeSerializer<T>`를 구현합니다. 프레임워크 내부에서 다음과 같이 매핑됩니다.
- **Redis 구현체**: `BridgeSerializer` -> `toJson/fromJson`을 거쳐 `Gson TypeAdapter`로 자동 매핑.
- **ProtoWeaver 구현체**: `BridgeSerializer` -> `BridgeSerializerAdapter`를 통해 ProtoWeaver의 `ObjectSerializer` 구조에 병합.
