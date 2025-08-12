package pers.fz.media.compressor.video;

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
public class ThreadExecutorVideo extends ThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 1;
    //以CPU总数*2作为线程池上限
    private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() *2;
    private static final int KEEP_ALIVE_TIME = 10;
    private static ThreadExecutorVideo executor;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
        }
    };

    public ThreadExecutorVideo(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public static ThreadExecutorVideo getInstance(){
        if(null == executor){
            synchronized (ThreadExecutorVideo.class){
                if(null == executor){
                    executor = new ThreadExecutorVideo(CORE_POOL_SIZE , MAXI_MUM_POOL_SIZE , KEEP_ALIVE_TIME , TimeUnit.SECONDS , new SynchronousQueue<Runnable>() , sThreadFactory);;
                }
            }
        }
        return executor;
    }
}


