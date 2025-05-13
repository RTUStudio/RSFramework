package kr.rtuserver.framework.bukkit.api.core.scheduler;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public interface Scheduler {

    void run(RSPlugin plugin, Consumer<ScheduledTask> consumer);

    ScheduledTask run(RSPlugin plugin, Runnable runnable);

    void runLater(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay);

    ScheduledTask runLater(RSPlugin plugin, Runnable runnable, long delay);

    void runTimer(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period);

    ScheduledTask runTimer(RSPlugin plugin, Runnable runnable, long delay, long period);

    void runAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer);

    ScheduledTask runAsync(RSPlugin plugin, Runnable runnable);

    void runLaterAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay);

    ScheduledTask runLaterAsync(RSPlugin plugin, Runnable runnable, long delay);

    void runTimerAsync(RSPlugin plugin, Consumer<ScheduledTask> consumer, long delay, long period);

    ScheduledTask runTimerAsync(RSPlugin plugin, Runnable runnable, long delay, long period);

    void run(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer);

    ScheduledTask run(RSPlugin plugin, Location location, Runnable runnable);

    void runLater(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay);

    ScheduledTask runLater(RSPlugin plugin, Location location, Runnable runnable, long delay);

    void runTimer(RSPlugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period);

    ScheduledTask runTimer(RSPlugin plugin, Location location, Runnable runnable, long delay, long period);

    void run(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer);

    ScheduledTask run(RSPlugin plugin, Entity entity, Runnable runnable);

    void runLater(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay);

    ScheduledTask runLater(RSPlugin plugin, Entity entity, Runnable runnable, long delay);

    void runTimer(RSPlugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period);

    ScheduledTask runTimer(RSPlugin plugin, Entity entity, Runnable runnable, long delay, long period);

}
