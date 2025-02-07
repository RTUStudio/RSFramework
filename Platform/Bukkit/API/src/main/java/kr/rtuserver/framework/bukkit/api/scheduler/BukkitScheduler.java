package kr.rtuserver.framework.bukkit.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BukkitScheduler implements RSScheduler {

    private final BukkitTask task;

    /**
     * Returns a task that will run on the next server tick.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param task   the task to be run
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     */
    @NotNull
    public static BukkitScheduler run(@NotNull Plugin plugin, @NotNull Runnable task) {
        return new BukkitScheduler(Bukkit.getScheduler().runTask(plugin, task));
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit. Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     * <p>
     * Returns a task that will run Async.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param task   the task to be run
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     */
    @NotNull
    public static BukkitScheduler runAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        return new BukkitScheduler(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    /**
     * Returns a task that will run after the specified number of server
     * ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     */
    @NotNull
    public static BukkitScheduler runLater(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        return new BukkitScheduler(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit. Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     * <p>
     * Returns a task that will run Async after the specified number
     * of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     */
    @NotNull
    public static BukkitScheduler runLaterAsync(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        return new BukkitScheduler(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay));
    }

    /**
     * Returns a task that will repeatedly run until cancelled, starting after
     * the specified number of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task
     * @param period the ticks to wait between runs
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     */
    @NotNull
    public static BukkitScheduler runTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        return new BukkitScheduler(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit. Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     * <p>
     * Returns a task that will repeatedly run Async until cancelled,
     * starting after the specified number of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task for the first
     *               time
     * @param period the ticks to wait between runs
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     */
    @NotNull
    public static BukkitScheduler runTimerAsync(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        return new BukkitScheduler(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period));
    }

    @Override
    public boolean cancel() {
        this.task.cancel();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return this.task.isCancelled();
    }
}
