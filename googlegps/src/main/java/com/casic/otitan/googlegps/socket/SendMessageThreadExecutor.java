package com.casic.otitan.googlegps.socket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fz on 2023/5/31 9:19
 * describe :线程池
 */
public class SendMessageThreadExecutor extends ThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 1;
    /**
     * 以CPU总数*2作为线程池上限
     */
    private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 3;
    private static volatile SendMessageThreadExecutor executor;

    private static final ThreadFactory S_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
        }
    };

    public SendMessageThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    //单例模式
    public static SendMessageThreadExecutor getInstance() {
        if (null == executor) {
            synchronized (SendMessageThreadExecutor.class) {
                if (null == executor) {
                    executor = new SendMessageThreadExecutor(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<>(),
                            S_THREAD_FACTORY);
                }
            }
        }
        return executor;
    }
}
