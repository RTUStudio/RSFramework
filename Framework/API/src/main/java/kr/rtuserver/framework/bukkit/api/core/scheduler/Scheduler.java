package kr.rtuserver.framework.bukkit.api.core.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public interface Scheduler {

    void sync(Plugin plugin, Consumer<ScheduledTask> consumer);

    ScheduledTask sync(Plugin plugin, Runnable runnable);

    void delay(Plugin plugin, Consumer<ScheduledTask> consumer, long delay, boolean async);

    ScheduledTask delay(Plugin plugin, Runnable runnable, long delay, boolean async);

    void repeat(Plugin plugin, Consumer<ScheduledTask> consumer, long delay, long period, boolean async);

    ScheduledTask repeat(Plugin plugin, Runnable runnable, long delay, long period, boolean async);

    void async(Plugin plugin, Consumer<ScheduledTask> consumer);

    ScheduledTask async(Plugin plugin, Runnable runnable);

    void sync(Plugin plugin, Location location, Consumer<ScheduledTask> consumer);

    ScheduledTask sync(Plugin plugin, Location location, Runnable runnable);

    void delay(Plugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, boolean async);

    ScheduledTask delay(Plugin plugin, Location location, Runnable runnable, long delay, boolean async);

    void repeat(Plugin plugin, Location location, Consumer<ScheduledTask> consumer, long delay, long period, boolean async);

    ScheduledTask repeat(Plugin plugin, Location location, Runnable runnable, long delay, long period, boolean async);

    void sync(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer);

    ScheduledTask sync(Plugin plugin, Entity entity, Runnable runnable);

    void delay(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, boolean async);

    ScheduledTask delay(Plugin plugin, Entity entity, Runnable runnable, long delay, boolean async);

    void repeat(Plugin plugin, Entity entity, Consumer<ScheduledTask> consumer, long delay, long period, boolean async);

    ScheduledTask repeat(Plugin plugin, Entity entity, Runnable runnable, long delay, long period, boolean async);

}

