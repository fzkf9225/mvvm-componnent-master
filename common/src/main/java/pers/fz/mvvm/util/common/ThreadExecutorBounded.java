package pers.fz.mvvm.util.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fz on 2023/5/31 9:19
 * describe :线程池,核心线程为3，且边界跟更大
 */
public class ThreadExecutorBounded extends ThreadPoolExecutor {
    /**
     * 核心线程数量
     */
    private static final int CORE_POOL_SIZE = 3;
    /**
     * 以CPU总数*2作为线程池上限，比如系统为4核cpu，那么最大线程数为8，因为核心线程设置为3了，因此非核心线程数量为5，
     */
    private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * 线程空闲等待销毁时间，单位秒，这里是空闲5秒后会自动销毁
     */
    private static final int KEEP_ALIVE_TIME = 5;
    private static volatile ThreadExecutorBounded executor;

    /**
     * 等待队列大小
     */
    private static int QUEUE_SIZE = 128;

    private static final ThreadFactory S_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
        }
    };

    public ThreadExecutorBounded(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,handler);
    }

    public static ThreadExecutorBounded getInstance() {
        if (null == executor) {
            synchronized (ThreadExecutorBounded.class) {
                if (null == executor) {
                    //SynchronousQueue+AbortPolicy更加高效，但是超过线程池大小会报错，LinkedBlockingDeque线程池大小越大也就越低效，但是超过线程池大小不会报错，他会排队执行
                    executor = new ThreadExecutorBounded(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE),
                            S_THREAD_FACTORY, new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return executor;
    }
}
