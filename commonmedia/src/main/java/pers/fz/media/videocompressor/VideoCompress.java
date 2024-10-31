package pers.fz.media.videocompressor;

import android.content.Context;
import android.net.Uri;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fz
 * Date: 2017/8/16
 * Time: 15:15
 */

public class VideoCompress {
    private static final String TAG = VideoCompress.class.getSimpleName();

    public static void compressVideoHigh(Context context,Uri srcPath, String destPath, CompressListener listener) {
        ThreadExecutor.getInstance().execute(new CompressRunnable(context,srcPath,destPath,listener,VideoController.COMPRESS_QUALITY_HIGH));
    }

    public static void compressVideoMedium(Context context,Uri srcPath, String destPath, CompressListener listener) {
        ThreadExecutor.getInstance().execute(new CompressRunnable(context,srcPath,destPath,listener,VideoController.COMPRESS_QUALITY_MEDIUM));
    }

    public static void compressVideoLow(Context context,Uri srcPath, String destPath, CompressListener listener) {
        ThreadExecutor.getInstance().execute(new CompressRunnable(context,srcPath,destPath,listener,VideoController.COMPRESS_QUALITY_LOW));
    }

    private static class CompressRunnable implements Runnable {
        private CompressListener mListener;
        private int mQuality;
        private Uri srcPath;
        private String destPath;
        private Context context;

        public CompressRunnable(Context context,Uri srcPath, String destPath, CompressListener mListener, int mQuality) {
            this.context = context;
            this.srcPath = srcPath;
            this.destPath = destPath;
            this.mListener = mListener;
            this.mQuality = mQuality;
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        public void run() {
            boolean result = VideoController.getInstance().convertVideo(context,srcPath, destPath, mQuality, new VideoController.CompressProgressListener() {
                @Override
                public void onProgress(float percent) {
                    if (mListener != null) {
                        mListener.onProgress(percent);
                    }
                }
            });
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess();
                } else {
                    mListener.onFail();
                }
            }
        }
    }

    /**
     * @author fz
     * @dec 开启线程, 进行压缩
     * @date 2023/4/26 16:23
     */
    public static class ThreadExecutor extends ThreadPoolExecutor {
        private static final int CORE_POOL_SIZE = 1;
        //以CPU总数*2作为线程池上限
        private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
        private static final int KEEP_ALIVE_TIME = 3;
        private static volatile ThreadExecutor executor;

        private static final ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
            }
        };

        public ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        //单例模式
        public static ThreadExecutor getInstance() {
            if (null == executor) {
                synchronized (ThreadExecutor.class) {
                    if (null == executor) {
                        executor = new ThreadExecutor(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                                sThreadFactory);
                    }
                }
            }
            return executor;
        }
    }

    public interface CompressListener {
        void onStart();

        void onSuccess();

        void onFail();

        void onProgress(float percent);
    }
}
