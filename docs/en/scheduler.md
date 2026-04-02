# Scheduler

RSFramework provides a unified scheduler system spanning Bukkit, Paper, and Folia. Instead of directly managing `BukkitRunnable` or `Bukkit.getScheduler()`, you can implement intuitive async processing using a Fluent API chaining or `CompletableFuture`-based modern approach.

---

## 1. Delayed/Repeating Chain Tasks

Define tasks in chain form via `CraftScheduler` for readable thread-switching logic. All tasks belonging to a plugin are automatically cancelled when the plugin is disabled, reducing memory leak concerns.

```java
// Execute on main thread, then delayed sync execution
CraftScheduler.sync(plugin, task -> {
    player.setHealth(20);
    // ... main thread code ...
}).delay(task -> {
    // Execute on main thread after 20 ticks (1 second)
    player.setHealth(1);
}, 20L);
```

### Folia Location/Entity-specific Scheduling

In Folia environments, threads are split by region or entity, so you must pass a **specific target** instead of global tick. RSFramework automatically maps to Folia's `RegionScheduler` or `EntityScheduler` when `Location` or `Entity` is passed as the first parameter. (In Spigot/Paper, it runs on the main thread as usual — fully compatible.)

```java
// Execute task within the thread region where the spider is located
CraftScheduler.sync(spider, task -> {
    spider.setTarget(player);
});

// Schedule in specific world coordinate space
CraftScheduler.delay(location, task -> {
    location.getBlock().setType(Material.DIAMOND_BLOCK);
}, 60L /* 3 seconds later */);
```

---

## 2. Safe Sync Result Return: `callSync` (CompletableFuture)

Spigot/Paper's `callSyncMethod()` was commonly used for data reads but is unavailable in Folia (`UnsupportedOperationException`). It also risks blocking the main thread indefinitely.

`CraftScheduler.callSync` was introduced to solve these issues. It immediately returns a **`CompletableFuture<T>`** to safely receive results without thread blocking.

### Best Practices
- **Never force `join()`.** Use `thenAccept` chaining on the returned `CompletableFuture` for callback processing.
- **Fully modern.** The framework automatically integrates Folia's region tick splitting without requiring separate compilation or branching.

**✅ [Recommended] Consume results asynchronously with `thenAccept`:**
```java
// Gather info immediately on main thread or Folia global thread
CraftScheduler.callSync(() -> {
    return Bukkit.getOnlinePlayers().size();
}).thenAccept(playerCount -> {
    System.out.println("Current player count: " + playerCount);
});

// Safely read data from a specific chest (Location)
Location chestLoc = new Location(world, 100, 64, 100);

CraftScheduler.callSync(chestLoc, () -> {
    Block block = chestLoc.getBlock();
    if (block.getState() instanceof Chest chest) {
        return chest.getInventory().getContents();
    }
    return new ItemStack[0];
}).thenAccept(items -> {
    player.sendMessage("Item types: " + items.length);
}).exceptionally(ex -> {
    player.sendMessage("Failed to access chest or thread.");
    return null;
});
```

**⚠️ [Caution] Risks of using `join()` or `get()`**
Only use `callSync(...).join()` inside a **pure async background thread** (e.g., DB save thread, external communication receiver, etc.).
If the in-game main/region thread (Tick Thread) calls `join()` while active, the server may experience severe lag or Folia's WatchDog may forcefully terminate the server due to thread deadlock.

---

## 3. QuartzScheduler (Scheduled/Repeating Jobs)

Use the Quartz scheduler with Cron expressions for reward or reset systems that need to run at specific times (e.g., every hour, midnight).

```java
// Call MyJob.class every day at midnight (0:00:00)
QuartzScheduler.run("DailyReset", "0 0 0 * * ?", MyJob.class);
```
※ `MyJob.class` should implement the Quartz API's `Job` interface.
