package kr.rtustudio.framework.bukkit.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Quartz Cron 스케줄러를 간편하게 사용할 수 있는 래퍼 클래스입니다.
 *
 * <p>Cron 표현식으로 {@link Job}을 등록하고, 다음 실행까지 남은 시간 조회 및 취소를 지원합니다. 동일한 {@link Job} 클래스는 하나의 {@link
 * JobDetail}로 공유되며, 여러 트리거를 붙일 수 있습니다.
 */
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuartzScheduler {

    private static final Map<JobKey, JobDetail> jobs = new HashMap<>();
    private static Scheduler scheduler;
    private final Trigger trigger;

    /**
     * Cron 스케줄을 생성하고 즉시 시작한다.
     *
     * @param name 트리거 식별자
     * @param cron Cron 표현식 (예: {@code "0 0/5 * * * ?"})
     * @param job 실행할 {@link Job} 클래스
     * @throws SchedulerException 스케줄러 등록 실패 시
     */
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

    /**
     * Cron 스케줄을 생성하고 즉시 시작한다. 실패 시 {@code null}을 반환한다.
     *
     * @param name 트리거 식별자
     * @param cron Cron 표현식
     * @param job 실행할 {@link Job} 클래스
     * @return 스케줄러 인스턴스, 실패 시 {@code null}
     */
    public static QuartzScheduler run(String name, String cron, Class<? extends Job> job) {
        try {
            return new QuartzScheduler(name, cron, job);
        } catch (SchedulerException e) {
            return null;
        }
    }

    private JobDetail createOrGet(Class<? extends Job> job) {
        JobDetail newJob = JobBuilder.newJob(job).usingJobData(new JobDataMap()).build();
        if (jobs.containsKey(newJob.getKey())) return jobs.get(newJob.getKey());
        jobs.put(newJob.getKey(), newJob);
        return newJob;
    }

    /**
     * 다음 실행까지 남은 시간을 밀리초로 반환한다.
     *
     * @return 남은 밀리초, 스케줄러가 실행 중이 아니면 {@code -1}
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

    /**
     * 다음 실행까지 남은 시간을 시/분/초로 분해하여 반환한다.
     *
     * @return 남은 시간, 스케줄러가 실행 중이 아니면 {@code null}
     */
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

    /**
     * 스케줄을 취소한다.
     *
     * @return 취소 성공 여부
     */
    public boolean cancel() {
        try {
            scheduler().unscheduleJob(trigger.getKey());
            return true;
        } catch (SchedulerException e) {
            log.warn("Failed to cancel scheduled task", e);
            return false;
        }
    }

    /**
     * 스케줄이 취소되었는지 확인한다.
     *
     * @return 취소 여부
     */
    public boolean isCancelled() {
        try {
            return !scheduler().checkExists(trigger.getKey());
        } catch (SchedulerException e) {
            log.warn("Failed to check scheduled task existence", e);
            return false;
        }
    }

    /**
     * 남은 시간을 시/분/초로 보관하는 레코드.
     *
     * @param hours 시간
     * @param minutes 분
     * @param seconds 초
     */
    public record Time(long hours, long minutes, long seconds) {}
}
