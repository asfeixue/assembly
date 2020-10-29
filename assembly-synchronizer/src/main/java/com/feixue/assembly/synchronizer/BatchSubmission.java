package com.feixue.assembly.synchronizer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class BatchSubmission {

    private static final int DEFAULT_CAPACITY = 16;
    private static final int DEFAULT_TIMEOUT = 50;
    private static Map<String, BatchSubmission> BATCH_SUBMISSION = new ConcurrentHashMap<>();

    private String key;
    private AtomicBoolean finishTag = new AtomicBoolean(false);
    private ArrayBlockingQueue<TaskContext> queue;

    private Lock lock = new ReentrantLock();
    private Condition masterCondition = lock.newCondition();

    public BatchSubmission(String key, TaskContext request) {
        this.key = key;

        this.queue = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
        this.queue.add(request);
    }

    public BatchSubmission(String key, TaskContext request, int capacity) {
        this.key = key;

        this.queue = new ArrayBlockingQueue<>(capacity);
        this.queue.add(request);
    }

    public static <V> SubmissionMaster getSubmissionMaster(String key, TaskContext request) {
        return getSubmissionMaster(key, request, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS, DEFAULT_CAPACITY);
    }

    public static <V> SubmissionMaster getSubmissionMaster(String key, TaskContext request, int timeout, TimeUnit timeUnit) {
        return getSubmissionMaster(key, request, timeout, timeUnit, DEFAULT_CAPACITY);
    }

    public static <V> SubmissionMaster getSubmissionMaster(String key, TaskContext request, int timeout, TimeUnit timeUnit, int queueCapacity) {
        BatchSubmission batchSubmission = BATCH_SUBMISSION.get(key);
        if (batchSubmission == null) {
            BatchSubmission submission = new BatchSubmission(key, request, queueCapacity);
            batchSubmission = BATCH_SUBMISSION.putIfAbsent(key, submission);
            if (batchSubmission == null) {
                submission.waitSlave(timeout, timeUnit);
                return SubmissionMaster.master(submission);
            } else {
                return SubmissionMaster.slave(batchSubmission);
            }
        } else {
            return SubmissionMaster.slave(batchSubmission);
        }
    }

    /**
     * 加入失败，需要自行处理
     * case1：批次容器已经执行完成，不能往一个已经完成的容器添加任务
     * case2：批次容器未执行完成，但是容量已经满负荷，不能继续添加
     * @param request
     * @return
     */
    public boolean offerRequest(TaskContext request) {
        if (finishTag.get()) {
            //获取到的批次容器已经执行结束，加入批次失败，需要线程自行处理
            return false;
        }
        while (true) {
            if (lock.tryLock()) {
                try {
                    if (finishTag.get()) {
                        //获取到了批次容器，又获取到了锁，但是容器已经执行结束，加入批次失败，需要线程自行处理
                        return false;
                    }
                    boolean offerResult = this.queue.offer(request);
                    if (!offerResult) {
                        //队列已满，通知master，避免master因wait时间太长还在无意义的等待
                        masterCondition.signal();
                    }
                    return offerResult;
                } finally {
                    lock.unlock();
                }
            } else {
                if (finishTag.get()) {
                    //获取到了批次容器，但是容器已经执行结束，加入批次失败，需要线程自行处理
                    return false;
                } else {
                    //如果竞争激烈，自旋一小会也就完成了
                }
            }
        }
    }

    /**
     * 获取slave批量的执行任务并进行后续处理
     * @return
     */
    public List<TaskContext> getSlaveRequestList() {
        lock.lock();
        try {
            //标记批次容器触发了提取动作，拒绝后续的元素加入队列
            finishTag.compareAndSet(false, true);

            return queue.stream().collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * master稍微等待一会，收集一波密集请求
     * case1：队列已满被唤醒
     * case2：等待超时
     *
     * 如果被中断，会将自己从集合中移除，同时将已加入队列中的任务中断唤醒。后续处理需要自行决定如何操作
     * @param timeout
     * @param timeUnit
     */
    private void waitSlave(int timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            masterCondition.await(timeout, timeUnit);
        } catch (InterruptedException interruptedException) {
            log.error("master wait slave failure!", interruptedException);
            //已经加入队列中的任务，需要主动唤醒让其自行决定
            queue.stream().forEach(taskContext -> taskContext.notifyInterrupt());
            throw new RuntimeException(interruptedException);
        } finally {
            //任意形式醒来，都需要把自己从缓存中移除
            BATCH_SUBMISSION.remove(key);
            lock.unlock();
        }
    }

    @Data
    @NoArgsConstructor
    public static class SubmissionMaster {

        private boolean isMaster;

        private BatchSubmission batchSubmission;

        public static SubmissionMaster master(BatchSubmission submission) {
            SubmissionMaster submissionMaster = new SubmissionMaster();
            submissionMaster.setMaster(true);
            submissionMaster.setBatchSubmission(submission);

            return submissionMaster;
        }

        public static SubmissionMaster slave(BatchSubmission submission) {
            SubmissionMaster submissionMaster = new SubmissionMaster();
            submissionMaster.setMaster(false);
            submissionMaster.setBatchSubmission(submission);

            return submissionMaster;
        }
    }

    @NoArgsConstructor
    public static class TaskContext<V, R> {

        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        private V request;

        private volatile R response;

        private volatile boolean interrupt = false;

        private AtomicBoolean finishTag = new AtomicBoolean(false);

        public V getRequest() {
            return request;
        }

        public void setRequest(V request) {
            this.request = request;
        }

        public R getResponse() {
            return response;
        }

        public boolean isInterrupt() {
            return interrupt;
        }

        public void notifyInterrupt() {
            lock.lock();
            try {
                this.interrupt = true;
                finishTag.compareAndSet(false, true);
                //主动唤醒，避免无意义的等待
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        public void notifyResponse(R response) {
            lock.lock();
            try {
                this.response = response;
                finishTag.compareAndSet(false, true);
                //主动唤醒，避免无意义的等待
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        public void waitNotify() {
            while (true) {
                if (finishTag.get()) {
                    break;
                }
                if (lock.tryLock()) {
                    try {
                        if (finishTag.get()) {
                            break;
                        }

                        condition.await(20, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException interruptedException) {
                        throw new RuntimeException(interruptedException);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }
}
