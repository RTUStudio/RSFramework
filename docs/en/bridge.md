# Bridge System Guide

> RSFramework's **Bridge** is a real-time communication layer between proxy and backend servers.
> It supports **Pub/Sub** broadcast and **RPC** request-response.

---

## 1. Architecture

### Interface Hierarchy

```
Bridge (isConnected + close)
├── Broadcast (Pub/Sub: register · subscribe · publish · unsubscribe)
├── Transaction (RPC: request · respond · getRequestTimeout)
│
├── Redis extends Broadcast
└── Proxium extends Broadcast, Transaction
```

| Interface | Key Features |
|-----------|-------------|
| `Bridge` | `isConnected` · `close` |
| `Broadcast` | `register` · `subscribe` · `publish` · `unsubscribe` |
| `Transaction` | `request` · `respond` · `getRequestTimeout` |
| `Proxium` | `getName` · `getNode` · `getPlayers` · `getPlayer` |

### Getting an Instance

```java
import kr.rtustudio.bridge.proxium.api.Proxium;

// Inside RSPlugin
Proxium proxium = getBridge(Proxium.class);
```

---

## 2. Channel and Type Registration

All communication operates on a **channel (`BridgeChannel`)** basis.
Data classes used on a channel must be **registered** to enable serialization/deserialization.

### Creating a Channel

```java
import kr.rtustudio.bridge.BridgeChannel;

// Plugin-specific channel (namespace:key format)
BridgeChannel channel = BridgeChannel.of("myplugin", "economy");

// Built-in channel constants
BridgeChannel.INTERNAL;   // rsframework:internal — framework internal communication
BridgeChannel.AUDIENCE;   // rsframework:audience — broadcast messages
```

### Manual Registration

```java
proxium.register(channel, BalanceRequest.class, BalanceResponse.class);
```

> [!TIP]
> **Auto-registration** — Passing types to `subscribe`, `request`, or `respond` **automatically registers** them.
> In most cases, you don't need to call `register()` directly.
>
> | Method | Auto-registered |
> |--------|----------------|
> | `subscribe(channel, Type.class, handler)` | Subscription message type |
> | `request(...).on(Type.class, handler)` | Request object + response type |
> | `respond(channel).on(Type.class, handler)` | Incoming request type |

> [!IMPORTANT]
> **Fory serialization constraint** — Type registration must be performed **immediately after server start** (onEnable, etc.).
> Registering new types after the server has already started communicating may cause Fory's late-registration errors.
> Call `subscribe()`, `request()`, `respond()` as early as possible since they auto-register types.

---

## 3. Pub/Sub (Publish / Subscribe)

Broadcasts messages to **all nodes** subscribed to a channel.
This is a `Bridge` interface feature, so it works identically for both Proxium and Redis.

### Subscribe

```java
// No register() call needed — auto-registered inside subscribe
proxium.subscribe(channel, TradePacket.class, trade -> {
    player.sendMessage(trade.getSender() + " requested to trade " + trade.getItem());
});

// Multiple type handlers can be registered individually on the same channel
proxium.subscribe(channel, ChatPacket.class, chat -> {
    Bukkit.broadcast(Component.text("[Chat] " + chat.message()));
});
```

> [!NOTE]
> Calling `subscribe()` again with the same channel and same type **replaces** the existing handler.
> A different type **adds** to existing handlers.

### Publish

```java
proxium.publish(channel, new TradePacket("IPECTER", "Diamond", 64));
```

### Unsubscribe

```java
proxium.unsubscribe(channel);
```

---

## 4. RPC (Remote Procedure Call)

Proxium-exclusive feature. Sends a **1:1 request to a specific server and receives a response.**

### 4.1. Data Class Definition

Define request/response classes for RPC. POJOs, records, or any form is supported.

```java
// Request
public record BalanceRequest(UUID uuid) {}

// Response
public record BalanceResponse(UUID uuid, double balance) {}
```

### 4.2. Response Handler Registration — `respond()`

Called on the server that **holds the data**.
`respond(channel)` returns a `ResponseContext`, chain `.on()` for type-specific handlers.

```java
proxium.respond(channel)
    .on(BalanceRequest.class, (sender, request) -> {
        // sender = name of the requesting server (e.g. "Lobby-1")
        double balance = getBalance(request.uuid());
        return new BalanceResponse(request.uuid(), balance);
    })
    .on(TransferRequest.class, (sender, request) -> {
        boolean ok = transfer(request.from(), request.to(), request.amount());
        return new TransferResponse(ok, ok ? "Success" : "Insufficient balance");
    })
    .error(e -> {
        // When an exception occurs during handler execution
        log.error("RPC response failed: [{}] {}", e.type(), e.getMessage());
    });
```

### 4.3. Sending Requests — `request()`

Called on the server that **needs the data**.
`request()` returns a `RequestContext`, chain `.on()` for response type handlers.

```java
// With explicit timeout
proxium.request("Survival-1", channel,
        new BalanceRequest(player.getUniqueId()),
        Duration.ofSeconds(5))
    .on(BalanceResponse.class, (sender, response) -> {
        player.sendMessage("Balance: " + response.balance());
    })
    .error(e -> {
        player.sendMessage("Request failed: " + e.type());
    });
```

### 4.4. CompletableFuture Conversion

When you want to work with `CompletableFuture` instead of fluent chaining:

```java
CompletableFuture<BalanceResponse> future = proxium
    .request("Survival-1", channel, new BalanceRequest(uuid), Duration.ofSeconds(5))
    .asFuture(BalanceResponse.class);

// Async follow-up
future.thenAccept(res -> player.sendMessage("Balance: " + res.balance()));
```

---

## 5. Error Handling

### RequestException

When RPC fails, a `RequestException` is passed to the `.error()` handler.

| Method | Returns | Description |
|--------|---------|-------------|
| `type()` | `ResponseStatus` | Failure cause classification |
| `cause()` | `Throwable?` | Cause exception (`getCause()`) |
| `getMessage()` | `String` | Error message |

### ResponseStatus

| Status | Meaning | Origin |
|--------|---------|--------|
| `SUCCESS` | Normal processing | — |
| `NO_HANDLER` | No handler for that type on target server | Remote |
| `ERROR` | Exception during handler execution | Remote |
| `TIMEOUT` | Response wait timeout | Local |

```java
.error(e -> {
    switch (e.type()) {
        case TIMEOUT    -> player.sendMessage("Server response timed out");
        case NO_HANDLER -> player.sendMessage("Service not found");
        case ERROR      -> {
            player.sendMessage("Server internal error");
            log.error("Cause: ", e.cause());
        }
    }
});
```

---

## 6. Player API

Query information about players connected to the proxy.

```java
// Get all players
Map<UUID, ProxyPlayer> players = proxium.getPlayers();

// Get player details
ProxyPlayer player = proxium.getPlayer(uuid);
if (player != null) {
    String name = player.getName();
    ProxiumNode server = player.getNode();
}

// Get server node info
ProxiumNode node = proxium.getNode("Survival-1");
```

---

## 7. Cross-server Teleport

Teleport players across the proxy network to specific locations or other players.

### Routing

| Condition | How it's handled |
|-----------|-----------------|
| Command server = player server = target server | Native `player.teleport()` — no Proxium |
| Player server = target server (remote start) | Velocity → direct packet to that server |
| Player server ≠ target server | Velocity → server transfer → packet delivery |

### Player-to-player

```java
ProxyPlayer player = proxium.getPlayer(uuid);
ProxyPlayer target = proxium.getPlayer(targetUuid);
player.teleport(target);
```

### Location-based

```java
ProxiumNode node = proxium.getNode("Survival-1");
ProxyLocation location = new ProxyLocation(node, "world", 100.5, 64, -200.5);
player.teleport(location);
```

---

## 8. Important Notes

> [!IMPORTANT]
> - **Async execution** — `.on()` and `.error()` callbacks run on **Netty I/O threads**. Use `CraftScheduler.sync()` to switch to the main thread for Bukkit API calls.
> - **No main thread blocking** — Calling `asFuture().join()` on the main thread will freeze the server.
> - **Bilateral registration** — Both requesting and responding sides **must** use the same channel.
> - **Registration timing** — Call `subscribe()`, `respond()`, etc. at server start (onEnable) to register types as early as possible.
> - **RPC Benchmark & JIT Warmup** — Proxium internally supports Fory serialization's *async JIT compilation*. The first RPC call may take `50ms+` due to compilation overhead, but subsequent calls achieve **`2–4ms` RTT** (local) thanks to JIT caching.
> - **Auto-reconnect** — If the server connection drops (normal or abnormal), the proxy automatically attempts reconnection at configured intervals.

---

## 9. API Reference

### Bridge

| Method | Description |
|--------|-------------|
| `isConnected()` | Check network connection status |
| `close()` | Close the bridge |

### Broadcast (Pub/Sub)

| Method | Description |
|--------|-------------|
| `register(channel, types...)` | Bind data classes to a channel |
| `subscribe(channel, type, handler)` | Subscribe to a channel (per-type, auto-register) |
| `publish(channel, message)` | Broadcast a message |
| `unsubscribe(channel)` | Cancel subscription |

### Transaction (RPC)

| Method | Returns | Description |
|--------|---------|-------------|
| `request(target, channel, payload, timeout)` | `RequestContext` | RPC request (explicit timeout) |
| `respond(channel)` | `ResponseContext` | Register RPC response handler |
| `getRequestTimeout()` | `Duration` | Configured default timeout |

### Proxium (Broadcast + Transaction)

| Method | Returns | Description |
|--------|---------|-------------|
| `getName()` | `String` | Local server name |
| `getNode(name)` | `ProxiumNode?` | Look up server node by name |
| `getPlayers()` | `Map<UUID, ProxyPlayer>` | All connected players |
| `getPlayer(uuid)` | `ProxyPlayer?` | Look up player by UUID |
| `request(target, channel, payload)` | `RequestContext` | RPC request with default timeout |

### RequestContext — returned by `request()`

| Method | Returns | Description |
|--------|---------|-------------|
| `.on(Type.class, (sender, response) -> {})` | `RequestContext` | Response type handler (chaining) |
| `.error(e -> {})` | `RequestContext` | Error handler |
| `.asFuture(Type.class)` | `CompletableFuture<T>` | Future conversion |

### ResponseContext — returned by `respond()`

| Method | Returns | Description |
|--------|---------|-------------|
| `.on(Type.class, (sender, req) -> resp)` | `ResponseContext` | Request type handler (chaining) |
| `.error(e -> {})` | `ResponseContext` | Error handler |

### BridgeChannel

| Method/Constant | Description |
|-----------------|-------------|
| `BridgeChannel.of("namespace", "key")` | Create custom channel |
| `BridgeChannel.of("namespace:key")` | Parse channel from string |
| `BridgeChannel.INTERNAL` | Framework internal channel |
| `BridgeChannel.AUDIENCE` | Broadcast channel |
