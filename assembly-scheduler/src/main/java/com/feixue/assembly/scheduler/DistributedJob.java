package com.feixue.assembly.scheduler;

/**
 * 分布式调度器
 */
public interface DistributedJob extends BaseJob {

    /**
     * 获取任务类型
     * @return
     */
    JobType getJobType();
}
