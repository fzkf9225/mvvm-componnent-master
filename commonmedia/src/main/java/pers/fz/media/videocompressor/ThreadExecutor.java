package pers.fz.media.videocompressor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fz on 2023/4/26 15:47
 * describe :
 */
public class ThreadExecutor extends ThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 1;
    //以CPU总数*2作为线程池上限
    private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() *2;
    private static final int KEEP_ALIVE_TIME = 3;
    private static ThreadExecutor executor;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
        }
    };

    public ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    //单例模式
    public static ThreadExecutor getInstance(){
        if(null == executor){
            synchronized (ThreadExecutor.class){
                if(null == executor){
                    executor = new ThreadExecutor(CORE_POOL_SIZE , MAXI_MUM_POOL_SIZE , KEEP_ALIVE_TIME , TimeUnit.SECONDS , new SynchronousQueue<Runnable>() , sThreadFactory);;
                }
            }
        }
        return executor;
    }
}


