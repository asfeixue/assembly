package com.feixue.assembly.synchronizer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class BlockedFuture<V> {

    private static final int DEFAULT_TIME = 50;

    private static Map<String, BlockedFuture> FUTURE_MAP = new ConcurrentHashMap<>();
    private volatile V response;

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public static FutureMaster getFutureMaster(String key) {
        BlockedFuture blockedFuture = FUTURE_MAP.get(key);
        if (blockedFuture == null) {
            BlockedFuture future = new BlockedFuture();
            blockedFuture = FUTURE_MAP.putIfAbsent(key, future);
            if (blockedFuture == null) {
                return FutureMaster.master(future);
            } else {
                return FutureMaster.slave(blockedFuture);
            }
        } else {
            return FutureMaster.slave(blockedFuture);
        }
    }

    private void setResponse(V response) {
        this.response = response;
    }

    private void signalAll() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public static <V> void notifyResponse(String key, V response) {
        BlockedFuture blockedFuture = FUTURE_MAP.remove(key);
        if (blockedFuture != null) {
            blockedFuture.setResponse(response);
            blockedFuture.signalAll();
        }
    }

    public boolean isDone() {
        return response != null;
    }

    public V get() {
        try {
            return get(DEFAULT_TIME, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("get timeout for value!", e);
            return null;
        }
    }

    public V get(long timeout, TimeUnit unit) throws TimeoutException {
        //响应已经获取完成，直接返回
        if (isDone()) {
            return response;
        }

        while (true) {
            if (lock.tryLock()) {
                try {
                    long waitTime = unit.toMillis(timeout);
                    while (!isDone()) {
                        if (waitTime > DEFAULT_TIME) {
                            boolean elapse = condition.await(DEFAULT_TIME, TimeUnit.MILLISECONDS);
                            if (elapse) {
                                return response;
                            } else {
                                //在极端情况下，可能在把自己挂起前，master线程已经signal完成了，此时如果等待超时了，不妨检查下是不是响应已经完成了
                                if (isDone()) {
                                    return response;
                                } else {
                                    waitTime -= DEFAULT_TIME;
                                }
                            }
                        } else {
                            if (condition.await(waitTime, TimeUnit.MILLISECONDS)) {
                                return response;
                            } else {
                                if (isDone()) {
                                    return response;
                                } else {
                                    throw new TimeoutException("invoke future get timeout for time=" + timeout + ", name=" + unit.name());
                                }
                            }
                        }
                    }
                    return response;
                } catch (InterruptedException interruptedException) {
                    log.error("future get invoker interrupted!", interruptedException);
                    return response;
                } finally {
                    lock.unlock();
                }
            } else {
                //让线程更敏感，避免无意义的阻塞
                if (isDone()) {
                    return response;
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class FutureMaster {

        private boolean isMaster;

        private BlockedFuture blockedFuture;

        public static FutureMaster master(BlockedFuture blockedFuture) {
            FutureMaster futureMaster = new FutureMaster();
            futureMaster.setMaster(true);
            futureMaster.setBlockedFuture(blockedFuture);

            return futureMaster;
        }

        public static FutureMaster slave(BlockedFuture blockedFuture) {
            FutureMaster futureMaster = new FutureMaster();
            futureMaster.setMaster(false);
            futureMaster.setBlockedFuture(blockedFuture);

            return futureMaster;
        }
    }
}
