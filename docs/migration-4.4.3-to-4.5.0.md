# Migration Guide: 4.4.3 → 4.5.0

이 문서는 RSFramework 4.4.3에서 4.5.0으로 업데이트할 때 필요한 변경 사항을 설명합니다.

---

## 1. Bridge 인터페이스 분리

`Bridge` 인터페이스가 **Broadcast**(Pub/Sub)와 **Transaction**(RPC)으로 분리되었습니다.

### 변경된 인터페이스 구조

| 4.4.3 | 4.5.0 |
|-------|-------|
| `Bridge` (모든 메서드 포함) | `Bridge` (isConnected + close만) |
| — | `Broadcast extends Bridge` (register, subscribe, publish, unsubscribe) |
| — | `Transaction extends Bridge` (request, respond, getRequestTimeout) |
| `Redis extends Bridge` | `Redis extends Broadcast` |
| `Proxium extends Bridge` | `Proxium extends Broadcast, Transaction` |

### 코드 변경

사용자 코드에서 `Proxium`이나 `Redis` 타입을 직접 사용하고 있다면 **변경이 필요 없습니다.**

```java
// 기존처럼 구체 타입으로 사용 (변경 없음)
Proxium proxium = registry.get(Proxium.class);  // Broadcast + Transaction 모두 사용 가능
Redis redis = registry.get(Redis.class);         // Broadcast + Lock 사용 가능
```

만약 `Bridge` 타입을 직접 참조하던 코드가 있다면, `Broadcast` 또는 `Transaction`으로 변경해야 합니다.

---

## 2. Node 인터페이스 추가

서버 노드를 식별하는 `Node` 인터페이스가 Bridge Common에 추가되었습니다.

```java
// Bridge Common
public interface Node {
    @NonNull String name();
}

// Proxium API
public record ProxiumNode(...) implements Node { ... }
```

`Transaction.request(Node target, ...)` 시그니처를 통해 범용 RPC를 지원합니다.

---

## 3. RPC 타입 패키지 이동

RPC 관련 타입이 Proxium API → Bridge Common으로 이동되었습니다.

| 이전 패키지 | 새 패키지 |
|------------|----------|
| `kr.rtustudio.bridge.proxium.api.context.RequestContext` | `kr.rtustudio.bridge.context.RequestContext` |
| `kr.rtustudio.bridge.proxium.api.context.ResponseContext` | `kr.rtustudio.bridge.context.ResponseContext` |
| `kr.rtustudio.bridge.proxium.api.handler.ResponseHandler` | `kr.rtustudio.bridge.handler.ResponseHandler` |
| `kr.rtustudio.bridge.proxium.api.exception.RequestException` | `kr.rtustudio.bridge.exception.RequestException` |
| `kr.rtustudio.bridge.proxium.api.exception.ResponseStatus` | `kr.rtustudio.bridge.context.ResponseStatus` |

> **주의**: 이전 패키지의 클래스는 **삭제**되었습니다. 반드시 새 패키지로 import를 변경해야 합니다.

---

## 4. Disconnect 패킷 제거 (Breaking)

`Disconnect` 패킷이 완전히 제거되었습니다.

### 4.4.3 동작
| 상황 | 동작 |
|------|------|
| Disconnect 수신 | 재연결 **안 함** |
| Disconnect 없이 끊김 | **무한** 재시도 |

### 4.5.0 동작
| 상황 | 동작 |
|------|------|
| 연결 후 끊김 (원인 무관) | 설정된 횟수만큼 재시도 (기본: 무한) |
| 초기 연결 실패 | 설정된 횟수만큼 재시도 (기본: 무한) |

> **참고**: 서버가 정상 종료되든 크래시되든, 프록시는 동일하게 재시도합니다. velocity.toml에서 서버를 제거하면 재시도가 중단됩니다.

---

## 5. Proxium API 변경

| 4.4.3 | 4.5.0 |
|-------|-------|
| `proxium.getServer()` | `proxium.getName()` |
| `proxium.getServer("name")` | `proxium.getNode("name")` |
