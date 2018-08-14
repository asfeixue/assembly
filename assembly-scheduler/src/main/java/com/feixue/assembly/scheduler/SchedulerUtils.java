package com.feixue.assembly.scheduler;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class SchedulerUtils {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerUtils.class);

    private static String zkAddress;

    private static CuratorFramework client;

    static {
        InputStream inputStream = SchedulerUtils.class.getClassLoader().getResourceAsStream("scheduler.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (Exception e) {
            logger.error("load scheduler.properties file from classpath failure!");
            throw new RuntimeException(e);
        }
        zkAddress = properties.getProperty("zookeeper.address");
        if (StringUtils.isBlank(zkAddress)) {
            throw new RuntimeException("scheduler.properties zookeeper.address can't empty!");
        }

        initClient();
    }

    public static String getZkAddress() {
        return zkAddress;
    }

    public static CuratorFramework getClient() {
        return client;
    }

    private static void initClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(SchedulerUtils.getZkAddress())
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(6000)
                .connectionTimeoutMs(3000)
                .namespace("scheduler")
                .build();
        client.start();
    }

}
