package com.feixue.assembly.scheduler;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerLeaderSelector {

    private static final String PATH = "/leader/";

    private DistributedJob distributedJob;

    private LeaderSelector leaderSelector;

    private AtomicBoolean status = new AtomicBoolean(false);

    public SchedulerLeaderSelector(DistributedJob distributedJob) {
        this.distributedJob = distributedJob;
        this.leaderSelector = buildLeaderSelector(SchedulerUtils.getClient());
    }

    private LeaderSelector buildLeaderSelector(CuratorFramework client) {
        if (StringUtils.isBlank(distributedJob.getJobName())) {
            throw new IllegalArgumentException("jobName can't empty!");
        }
        if (distributedJob.getJobName().contains("/")) {
            throw new IllegalArgumentException("jobName can't contains '/' symbol.");
        }

        LeaderSelector leaderSelector = new LeaderSelector(client, PATH + distributedJob.getJobName(), new LeaderSelectorListener() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                distributedJob.execute();
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {

            }
        });

        return leaderSelector;
    }

    /**
     * 尝试选举，已选举，或者选举失败，返回false；选举成功，返回true
     * @return
     */
    public void requeue() {
        if (status.compareAndSet(false, true)) {
            leaderSelector.start();
        } else {
            leaderSelector.requeue();
        }
    }

    /**
     * 关闭选举器
     */
    public void close() {
        CloseableUtils.closeQuietly(leaderSelector);
    }
}
