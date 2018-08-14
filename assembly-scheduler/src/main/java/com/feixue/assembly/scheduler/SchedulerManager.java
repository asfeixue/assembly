package com.feixue.assembly.scheduler;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 调度管理器
 */
@Service
public class SchedulerManager implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerManager.class);

    @Resource
    private Scheduler cloudstackScheduler;

    private ExecutorService executors = new ThreadPoolExecutor(
            2, 10, 5L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(500), r -> new Thread(r, "schedulerThread"));

    private Map<String, SchedulerLeaderSelector> selectorMap = new ConcurrentHashMap<>();

    /**
     * 注册调度任务
     * @param baseJob
     * @param cron
     */
    public void registerJob(BaseJob baseJob, String cron) {
        String name = baseJob.getJobName();
        name += "-job";
        TriggerKey triggerKey = TriggerKey.triggerKey(name);
        try {
            CronTrigger trigger = (CronTrigger) cloudstackScheduler.getTrigger(triggerKey);
            if (trigger == null) {
                JobDetail jobDetail = JobBuilder.newJob(InternalJob.class)
                        .withIdentity(name)
                        .build();

                jobDetail.getJobDataMap().put("job", baseJob);
                if (baseJob instanceof DistributedJob) {
                    SchedulerLeaderSelector selector = new SchedulerLeaderSelector((DistributedJob) baseJob);
                    jobDetail.getJobDataMap().put("selector", selector);

                    selectorMap.put(baseJob.getJobName(), selector);
                }

                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(name)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
                cloudstackScheduler.scheduleJob(jobDetail, trigger);
                LOGGER.info("scheduler job: " + name);
            } else {
                trigger = trigger.getTriggerBuilder()
                        .withIdentity(triggerKey)
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(cron))
                        .build();
                cloudstackScheduler.rescheduleJob(triggerKey, trigger);
                LOGGER.info("rescheduler job: " + name);
            }
        } catch (SchedulerException e) {
            LOGGER.error("register job error!", e);
        }
    }

    /**
     * 一次性执行的任务，非定时任务
     * @param baseJob
     */
    public void registerJob(final BaseJob baseJob) {
        executors.execute(() -> baseJob.execute());
    }

    /**
     * 移除指定任务
     * @param name
     */
    public void unregisterJob(String name) {
        name += "-job";
        JobKey jobKey = JobKey.jobKey(name);
        TriggerKey triggerKey = TriggerKey.triggerKey(name);

        if (triggerKey == null || jobKey == null) {
            return;
        }

        try {
            cloudstackScheduler.pauseTrigger(triggerKey);
            cloudstackScheduler.unscheduleJob(triggerKey);
            cloudstackScheduler.deleteJob(jobKey);

            SchedulerLeaderSelector selector = selectorMap.get(name);
            if (selector != null) {
                selector.close();
            }

            LOGGER.info("remove job: " + name);
        } catch (SchedulerException e) {
            LOGGER.error("unRegister job error!", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        cloudstackScheduler.shutdown();
        executors.shutdown();

        for (SchedulerLeaderSelector selector : selectorMap.values()) {
            selector.close();
        }
    }

    private static class InternalJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            /**
             * 获取任务，并执行
             */
            BaseJob job = (BaseJob) context.getJobDetail().getJobDataMap().get("job");

            if (job instanceof DistributedJob) {
                SchedulerLeaderSelector selector = (SchedulerLeaderSelector) context.getJobDetail().getJobDataMap().get("selector");
                selector.requeue();
            } else {
                job.execute();
            }
        }

    }
}
