# RSFramework Storage System

A unified storage system that standardizes all databases under a **JSON document-based API** (`add` / `set` / `get`).

---

## 1. Supported Databases

| Type | Category | Notes |
|------|----------|-------|
| **JSON** | Local | `.json` file-based |
| **SQLITE** | Local | HikariCP-based `.db` file |
| **MYSQL** | RDBMS | mysql-connector |
| **MARIADB** | RDBMS | mariadb-java-client |
| **POSTGRESQL** | RDBMS | PostgreSQL driver |
| **MONGODB** | NoSQL | Async document cluster |

> [!TIP]
> Regardless of the `StorageType` selected, you use the **same `Storage` API**.
> Just change the type in the config file and the driver switches automatically.

---

## 2. Storage API

### Core Interface

```java
public interface Storage {
    CompletableFuture<Result> add(JSON data);       // Insert
    CompletableFuture<Result> set(JSON find, JSON data);  // Update
    CompletableFuture<List<JsonObject>> get(JSON find);    // Query
    void close();
}
```

### Result (Status)

| Status | `success` | `changed` | Meaning |
|--------|-----------|-----------|---------|
| `UPDATED` | ✅ | ✅ | Successfully changed |
| `UNCHANGED` | ✅ | ❌ | Succeeded but no changes |
| `FAILED` | ❌ | ❌ | Failed |

### JSON Builder

```java
import kr.rtustudio.storage.JSON;

JSON player = JSON.of("uuid", uuid.toString())
    .append("name", "IPECTER")
    .append("coins", 1000)
    .append("level", 5);

JsonObject json = player.get(); // Convert to JsonObject
```

---

## 3. Usage Guide

### 3.1. Registration

```java
@Override
protected void enable() {
    registerStorage("PlayerData", StorageType.MARIADB);
    registerStorage("Settings", StorageType.SQLITE);
    registerStorage("Cache"); // Default JSON
}
```

A config file `Config/Storage/PlayerData.yml` is auto-generated on registration:
```yaml
Host: '127.0.0.1'
Port: 3306
Username: 'root'
Password: 'password123'
Database: 'minecraft_server'
```

### 3.2. Dynamic Type from Config

```java
String dbType = getConfig().getString("database-type", "sqlite");
registerStorage("PlayerData", StorageType.fromString(dbType));
```

### 3.3. Accessing the Instance

```java
Storage storage = getStorage("PlayerData");
```

---

## 4. Practical Examples

### Player Data CRUD

```java
public class PlayerStorage {

    private final Storage storage;

    public PlayerStorage(RSPlugin plugin) {
        this.storage = plugin.getStorage("PlayerData");
    }

    // CREATE
    public void createPlayer(UUID uuid, String name) {
        storage.add(JSON.of("uuid", uuid.toString())
            .append("name", name)
            .append("coins", 0)
            .append("level", 1)
        ).thenAccept(result -> {
            if (result.isSuccess()) System.out.println("Player created");
        });
    }

    // READ
    public void getPlayer(UUID uuid) {
        storage.get(JSON.of("uuid", uuid.toString())).thenAccept(results -> {
            if (!results.isEmpty()) {
                int coins = results.get(0).get("coins").getAsInt();
                System.out.println("Coins: " + coins);
            }
        });
    }

    // UPDATE
    public void setCoins(UUID uuid, int newCoins) {
        JSON find = JSON.of("uuid", uuid.toString());
        JSON data = JSON.of("uuid", uuid.toString()).append("coins", newCoins);

        storage.set(find, data).thenAccept(result -> {
            if (result.isChanged()) System.out.println("Coins updated");
        });
    }
}
```

### Switching to Bukkit Main Thread

```java
public void showBalance(Player player) {
    storage.get(JSON.of("uuid", player.getUniqueId().toString())).thenAccept(results -> {
        // ⚠️ Running on async thread — thread switch required for Bukkit API
        CraftScheduler.sync(plugin, task -> {
            if (results.isEmpty()) {
                player.sendMessage("No data found.");
                return;
            }
            int coins = results.get(0).get("coins").getAsInt();
            player.sendMessage("Coins: " + coins);
        });
    });
}
```

---

## 5. Important Notes

> [!IMPORTANT]
> - **Async returns** — `add()`, `set()`, `get()` all return `CompletableFuture`. Use `CraftScheduler.sync()` for Bukkit API calls.
> - **No main thread blocking** — Calling `.join()` or `.get()` on the main thread can freeze the server.
> - **DB-independent** — The same code works with any backend: JSON, SQLite, MySQL, MongoDB, etc.
> - **Built-in HikariCP** — Connection pools are automatically created for relational DB registrations.
> - **Hot reload** — `/rsf reload` detects changed storage and reconnects only what's needed.

---

## 6. API Reference

### Storage

| Method | Returns | Description |
|--------|---------|-------------|
| `add(JSON data)` | `CompletableFuture<Result>` | Insert a document |
| `set(JSON find, JSON data)` | `CompletableFuture<Result>` | Search and update |
| `get(JSON find)` | `CompletableFuture<List<JsonObject>>` | Search and query |
| `close()` | `void` | Close storage |

### JSON (Builder)

| Method | Returns | Description |
|--------|---------|-------------|
| `JSON.of()` | `JSON` | Empty builder |
| `JSON.of(key, value)` | `JSON` | Builder with initial field |
| `append(key, value)` | `JSON` | Add field (chaining) |
| `get()` | `JsonObject` | Convert |

### RSPlugin (Storage)

| Method | Description |
|--------|-------------|
| `registerStorage(name, type)` | Register storage |
| `registerStorage(name)` | Register with JSON type |
| `getStorage(name)` | Get registered instance |
