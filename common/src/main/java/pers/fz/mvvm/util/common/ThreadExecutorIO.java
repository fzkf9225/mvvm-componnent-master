package pers.fz.mvvm.util.common;

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
public class ThreadExecutorIO extends ThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 3;
    /**
     * 以CPU总数*2作为线程池上限
     */
    private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int KEEP_ALIVE_TIME = 5;
    private static volatile ThreadExecutorIO executor;

    private static final ThreadFactory S_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
        }
    };

    public ThreadExecutorIO(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    //单例模式
    public static ThreadExecutorIO getInstance() {
        if (null == executor) {
            synchronized (ThreadExecutorIO.class) {
                if (null == executor) {
                    executor = new ThreadExecutorIO(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<>(),
                            S_THREAD_FACTORY);
                }
            }
        }
        return executor;
    }
}
