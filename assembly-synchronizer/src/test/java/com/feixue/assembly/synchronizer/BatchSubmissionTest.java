package com.feixue.assembly.synchronizer;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BatchSubmissionTest extends TestCase {

    @Test
    public void test0() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(500);

        String key = "1111111";

        for (int index = 0; index < 500; index++) {
            new Thread(() -> {
                BatchSubmission.TaskContext taskContext = new BatchSubmission.TaskContext();
                taskContext.setRequest(Thread.currentThread().getName());

                while (true) {
                    BatchSubmission.SubmissionMaster submissionMaster = BatchSubmission.getSubmissionMaster(key, taskContext);
                    if (submissionMaster.isMaster()) {
                        List<BatchSubmission.TaskContext> taskContextList = submissionMaster.getBatchSubmission().getSlaveRequestList();
                        taskContextList.stream().forEach(taskContext1 -> {
                            taskContext1.notifyResponse("这是响应！" + Thread.currentThread().getName());
                        });
                        break;
                    } else {
                        boolean offerResult = submissionMaster.getBatchSubmission().offerRequest(taskContext);
                        if (offerResult) {
                            taskContext.waitNotify();
                            Object response = taskContext.getResponse();
                            System.err.println(Thread.currentThread().getName() + "-" + response);
                            break;
                        }
                    }
                }

                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
    }
}