package com.feixue.assembly.scheduler;

/**
 * 单点调度器
 */
public interface BaseJob {
    /**
     * 执行具体的任务
     */
    void execute();

    /**
     * 获取任务唯一名称
     * @return
     */
    String getJobName();
}
