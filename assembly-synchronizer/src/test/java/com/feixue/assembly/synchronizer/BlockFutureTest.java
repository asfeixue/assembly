package com.feixue.assembly.synchronizer;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class BlockFutureTest extends TestCase {

    @Test
    public void test() throws InterruptedException, IOException {
        String key = "test";

        CountDownLatch countDownLatch = new CountDownLatch(200);
        for (int index = 0; index < 200; index++) {
            new Thread(() -> {
                BlockedFuture.FutureMaster futureMaster = BlockedFuture.getFutureMaster(key);
                if (futureMaster.isMaster()) {
                    //master才有权限继续执行，slave只能等待结果
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    BlockedFuture.notifyResponse(key, "这是响应结果哦！" + UUID.randomUUID().toString());
                } else {
                    try {
                        long start = System.currentTimeMillis();
                        Object response = futureMaster.getBlockedFuture().get(1, TimeUnit.MILLISECONDS);
                        long end = System.currentTimeMillis();

                        System.err.println(Thread.currentThread().getName() + " get response for" + response + " time " + (end - start));
                    } catch (TimeoutException e) {
                        //超时未获取到后，可以自行决定是自行独立获取，还是返回异常
                        log.error("future get response timeout!", e);
                    }
                }

                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        System.in.read();
    }
}