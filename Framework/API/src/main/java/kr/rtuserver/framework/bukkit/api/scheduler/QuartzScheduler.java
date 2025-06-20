package kr.rtuserver.framework.bukkit.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuartzScheduler {

    private final static Map<JobKey, JobDetail> jobs = new HashMap<>();
    private static Scheduler scheduler;
    private final Trigger trigger;

    public QuartzScheduler(String name, String cron, Class<? extends Job> job) throws SchedulerException {
        JobDetail detail = createOrGet(job);
        this.trigger = TriggerBuilder.newTrigger()
                .withIdentity("rsframework", name)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .forJob(detail).startNow().build();
        scheduler().scheduleJob(trigger);
    }

    private static Scheduler scheduler() throws SchedulerException {
        if (scheduler != null) return scheduler;
        Scheduler newScheduler = new StdSchedulerFactory().getScheduler();
        newScheduler.start();
        return newScheduler;
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

    public String nextFireTime() {
        try {
            Trigger trigger = scheduler().getTrigger(this.trigger.getKey());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextFireTime = trigger.getNextFireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            Duration duration = Duration.between(now, nextFireTime);

            long days = duration.toDays();
            long hours = duration.toHours() - days * 24;
            long minutes = duration.toMinutes() - days * 24 * 60 - hours * 60;
            return String.format("%d시간 %d분", hours, minutes);
        } catch (SchedulerException e) {
            return "0시간 0분";
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

}
