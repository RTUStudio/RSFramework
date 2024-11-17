package kr.rtuserver.framework.bukkit.api.utility.scheduler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuartzScheduler {

    public static void addTask(String name, String cron, Class<? extends Job> job) {
        JobDetail detail = JobBuilder.newJob(job).usingJobData(new JobDataMap()).build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("rsframework", name)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .forJob(detail)
                .startNow()
                .build();
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            if (!scheduler.isStarted()) scheduler.start();
            scheduler.scheduleJob(detail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static void removeTask(String name) {
        try {
            StdSchedulerFactory.getDefaultScheduler().unscheduleJob(TriggerKey.triggerKey("rsframework", name));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static String getRemainTime(String name) {
        try {
            Trigger trigger = StdSchedulerFactory.getDefaultScheduler().getTrigger(TriggerKey.triggerKey("rsframework", name));
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
}
