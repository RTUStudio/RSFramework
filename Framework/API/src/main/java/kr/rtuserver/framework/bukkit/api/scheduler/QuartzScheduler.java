package kr.rtuserver.framework.bukkit.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuartzScheduler {

    private static final Map<JobKey, JobDetail> jobs = new HashMap<>();
    private static Scheduler scheduler;
    private final Trigger trigger;

    public QuartzScheduler(String name, String cron, Class<? extends Job> job)
            throws SchedulerException {
        JobDetail detail = createOrGet(job);
        this.trigger =
                TriggerBuilder.newTrigger()
                        .withIdentity("rsframework", name)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                        .forJob(detail)
                        .startNow()
                        .build();
        scheduler().scheduleJob(trigger);
    }

    private static Scheduler scheduler() throws SchedulerException {
        if (scheduler != null) return scheduler;
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        return scheduler;
    }

    public static QuartzScheduler run(String name, String cron, Class<? extends Job> job) {
        try {
            return new QuartzScheduler(name, cron, job);
        } catch (SchedulerException e) {
            return null;
        }
    }

    private JobDetail createOrGet(Class<? extends Job> job) {
        JobDetail newJob = JobBuilder.newJob(job).usingJobData(new JobDataMap()).build();
        if (jobs.containsKey(newJob.getKey())) return newJob;
        return jobs.put(newJob.getKey(), newJob);
    }

    /**
     * Get remain milliseconds to next fire if scheduler is not running, return -1
     *
     * @return remain milliseconds to next fire
     */
    public long getRemainMilliseconds() {
        try {
            Trigger trigger = scheduler().getTrigger(this.trigger.getKey());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextFireTime =
                    trigger.getNextFireTime()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
            Duration duration = Duration.between(now, nextFireTime);
            return duration.toMillis();
        } catch (SchedulerException e) {
            return -1;
        }
    }

    public Time getRemainTime() {
        try {
            Trigger trigger = scheduler().getTrigger(this.trigger.getKey());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextFireTime =
                    trigger.getNextFireTime()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
            Duration duration = Duration.between(now, nextFireTime);
            long days = duration.toDays();
            long hours = duration.toHours() - days * 24;
            long minutes = duration.toMinutes() - days * 24 * 60 - hours * 60;
            long seconds =
                    duration.toSeconds() - days * 24 * 60 * 60 - hours * 60 * 60 - minutes * 60;
            return new Time(hours, minutes, seconds);
        } catch (SchedulerException e) {
            return null;
        }
    }

    public boolean cancel() {
        try {
            scheduler().unscheduleJob(trigger.getKey());
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isCancelled() {
        try {
            return !scheduler().checkExists(trigger.getKey());
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public record Time(long hours, long minutes, long seconds) {}
}
