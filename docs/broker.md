# Broker

서버 간 통신(Inter-Server Communication) 및 이벤트/메시지 전달을 위한 모듈입니다.
플러그인은 `kr.rtustudio.broker.Broker` 인터페이스를 통해 통신 대상 서버의 내부 구조를 몰라도 안전하게 이벤트를 주고받을 수 있습니다.

---

## 1. 지원하는 브로커 타입

RSFramework는 두 가지 브로커 구현체를 기본 제공합니다.
모든 직렬화는 기본적으로 **Apache Fury**를 사용하며, 선택적으로 **Snappy 압축**을 활성화할 수 있습니다.

| 브로커 | 패키지 | 특징 | 용도 |
|--------|--------|------|------|
| **Redisson** | `Broker:Redisson` | Redis Pub/Sub 기반 통신 | 물리적으로 분리된 여러 서버 간의 비동기 메시지 방송 |
| **ProtoWeaver** | `Broker:ProtoWeaver` | 프록시-백엔드 간 Netty 다이렉트 통신 | BungeeCord/Velocity 프록시와 Bukkit 서버 간의 양방향 패킷 통신 |

---

## 2. Broker 인터페이스

`kr.rtustudio.broker.Broker`

```java
public interface Broker {
    
    // 채널에 수신할 패킷(메시지) 타입 등록
    void register(String channel, Class<?>... types);
    
    // 채널 구독 및 리스너 등록
    void subscribe(String channel, Consumer<Object> handler);
    
    // 특정 채널로 객체 전송
    void publish(String channel, Object message);
    
    // 채널 구독 취소
    void unsubscribe(String channel);
    
    // 브로커 연결 종료
    void close();
}
```

---

## 3. Redisson Broker

Redis의 Pub/Sub 기능을 활용하여 메시지를 주고받는 브로커입니다.

### 3.1. 설정 파일 (`Config/Broker/Redis.yml`)
`ConfigPath.of("Broker", "Redis")` 경로에서 설정을 읽습니다.

```yaml
mode: SINGLE       # SINGLE | SENTINEL | CLUSTER
address: "redis://127.0.0.1:6379"
password: ""
database: 0
connection-minimum-idle-size: 5
connection-pool-size: 10
threads: 16
netty-threads: 32
tls: false
```

### 3.2. 사용 예시

```java
// 1. 메시지 타입(Record 또는 일반 클래스) 정의
public record ShopPacket(String itemId, int amount, double price) {}

@Override
protected void enable() {
    RedisBroker redis = getBroker(RedisBroker.class);
    
    // 2. 채널에 타입 등록 (직렬화를 위해 필수)
    redis.register("myplug:shop", ShopPacket.class);
    
    // 3. 채널 구독
    redis.subscribe("myplug:shop", packet -> {
        if (packet instanceof ShopPacket sp) {
            console("<yellow>Shop event: " + sp.itemId() + " x" + sp.amount());
        }
    });
    
    // 4. 메시지 발행 (이벤트 발생 시 등)
    redis.publish("myplug:shop", new ShopPacket("sword", 1, 100.0));
}
```

---

## 4. ProtoWeaver Broker

프록시(BungeeCord, Velocity)와 백엔드(Bukkit) 간에 패킷을 직접 송수신하는 고성능 브로커입니다.
내부적으로 Netty 기반 TCP 통신을 사용하며, TLS 암호화를 지원합니다.

### 4.1. 구조 및 원리

1. **Proxy 서버(Velocity/Bungee)**가 설정된 포트로 서버 소켓 오픈
2. **Backend 서버(Bukkit)**가 Proxy에 소켓 연결 (Handshake)
3. 인증 후, 양방향으로 `Packet` 송수신 가능

### 4.2. 설정 파일 (`Config/Broker/ProtoWeaver.yml`)

```yaml
tls:
  enabled: true    # Netty SslHandler (자체 서명 인증서 자동 생성)
compression: false # Snappy 압축 사용 여부
```

> **참고**: ProtoWeaver는 내부적으로 `rsframework:internal` 채널을 사용하여 서버 이름, 플레이어 목록, 브로드캐스트, 텔레포트 등의 내장 패킷을 프록시와 동기화합니다.

### 4.3. 사용 예시 (Bukkit 측)

```java
@Override
protected void enable() {
    // ProtoWeaver Bukkit API 객체 획득
    kr.rtustudio.broker.protoweaver.bukkit.api.ProtoWeaver pw = 
        getBroker(kr.rtustudio.broker.protoweaver.bukkit.api.ProtoWeaver.class);
    
    // 1. 채널 등록
    pw.register("myplug:proxy", TitlePacket.class);
    
    // 2. 구독 (Proxy에서 보낸 패킷 수신)
    pw.subscribe("myplug:proxy", packet -> {
        if (packet instanceof TitlePacket tp) {
            System.out.println("Received title packet from proxy");
        }
    });
    
    // 3. 패킷 발행 (Proxy로 전송)
    pw.publish("myplug:proxy", new TitlePacket(uuid, "Hello", "World"));
    
    // (선택) 프레임워크 내장 기능 사용
    pw.publish(new SendMessage(uuid, "<green>직접 메시지 전송</green>"));
}
```

---

## 5. BrokerOptions 및 직렬화

RSFramework는 `BrokerOptions`를 통해 직렬화 방식(Fury), 압축(Snappy), TLS 설정 등을 관리합니다.

```java
BrokerOptions options = BrokerOptions.builder(classLoader)
    .compress(true)   // Snappy 압축 활성화
    .tls(false)       // TLS 비활성화
    .build();

// 커스텀 옵션으로 브로커 수동 생성 가능
RedisBroker broker = new RedisBroker(redisConfig, options);
```

### 와이어 프레임 포맷
네트워크로 전송되는 데이터의 기본 구조:
```text
[4 bytes: 채널 이름 길이] + [채널명 UTF-8] + [Fury 직렬화 데이터 (선택적 Snappy 압축)]
```

### 커스텀 직렬화 (`BrokerSerializer`)
Bukkit 전용 객체(`ItemStack`, `Location` 등)는 기본 직렬화가 불가능하므로, 커스텀 시리얼라이저를 구현하여 등록할 수 있습니다.

```java
public class ItemStackSerializer implements BrokerSerializer<ItemStack> {
    @Override
    public byte[] serialize(ItemStack value) {
        return value.serializeAsBytes();
    }
    @Override
    public ItemStack deserialize(byte[] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }
}
```
